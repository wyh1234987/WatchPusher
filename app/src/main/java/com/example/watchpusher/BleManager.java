package com.example.watchpusher;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import java.util.Set;
import java.util.UUID;

public class BleManager {
    private static final String TAG = "BleManager";
    // 🎯 目标名字和地址 (从您的截图获取)
    private static final String TARGET_NAME = "HH-D101_Watch";
    private static final String TARGET_MAC  = "63:38:AD:CE:38:63"; 

    private static final UUID SERVICE_UUID = UUID.fromString("0000fe01-0000-1000-8000-00805f9b34fb");
    private static final UUID CHAR_UUID    = UUID.fromString("0000fe02-0000-1000-8000-00805f9b34fb");

    private BluetoothGatt mGatt;
    private BluetoothGattCharacteristic mNotifyChar;
    private Context mContext;
    private boolean mConnected = false;

    public BleManager(Context context) { this.mContext = context; }

    public void startScanAndConnect() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) return;

        // 1️⃣ 第一步：先检查“已配对设备” (解决截图中 BONDED 的问题)
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices != null) {
            for (BluetoothDevice device : pairedDevices) {
                if (TARGET_NAME.equals(device.getName()) || TARGET_MAC.equals(device.getAddress())) {
                    MainActivity.getInstance().updateStatus("STATUS: FOUND PAIRED", Color.BLUE);
                    connectToDevice(device);
                    return; // 找到了直接连，结束方法
                }
            }
        }

        // 2️⃣ 第二步：如果通讯录里没有，再开始扫描
        final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner == null) return;

        scanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                String name = (result.getScanRecord() != null) ? result.getScanRecord().getDeviceName() : device.getName();
                String mac = device.getAddress();

                if (TARGET_NAME.equals(name) || TARGET_MAC.equals(mac)) {
                    MainActivity.getInstance().updateStatus("FOUND: " + name, Color.parseColor("#0000AA"));
                    scanner.stopScan(this);
                    connectToDevice(device);
                }
            }
        });
    }

    private void connectToDevice(BluetoothDevice device) {
        // 使用 TRANSPORT_LE 强制使用低功耗模式连接，更稳定
        mGatt = device.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnected = true;
                MainActivity.getInstance().updateStatus("STATUS: CONNECTED", Color.parseColor("#00AA00"));
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnected = false;
                MainActivity.getInstance().updateStatus("STATUS: DISCONNECTED", Color.RED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    mNotifyChar = service.getCharacteristic(CHAR_UUID);
                }
            }
        }
    };

    public void sendMessage(String msg) {
        if (!mConnected || mNotifyChar == null) return;
        mNotifyChar.setValue(msg.getBytes());
        mGatt.writeCharacteristic(mNotifyChar);
    }
}

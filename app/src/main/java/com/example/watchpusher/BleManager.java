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
import java.util.UUID;

public class BleManager {
    private static final String TAG = "BleManager";
    private static final String DEVICE_NAME = "HH-D101_Watch";
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

        final BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (scanner == null) return;

        scanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                if (device != null && DEVICE_NAME.equals(device.getName())) {
                    scanner.stopScan(this);
                    mGatt = device.connectGatt(mContext, false, mGattCallback);
                }
            }
        });
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnected = true;
                // ✅ 连上变绿
                MainActivity.getInstance().updateStatus("STATUS: CONNECTED", Color.parseColor("#00AA00"));
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnected = false;
                // ❌ 断开变红
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

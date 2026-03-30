package com.example.watchpusher;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private static MainActivity instance;
    private BleManager mBleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        mBleManager = new BleManager(this);

        // 按钮 1：授权通知
        Button btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        });

        // 按钮 2：连接蓝牙 (增加权限检查)
        Button btnConnect = findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(v -> {
            if (checkBlePermissions()) {
                mBleManager.startScanAndConnect();
                Toast.makeText(this, "Scanning for Watch...", Toast.LENGTH_SHORT).show();
            } else {
                requestBlePermissions();
            }
        });
    }

    private boolean checkBlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                   ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestBlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }
    }

    public static MainActivity getInstance() { return instance; }
    public BleManager getBleManager() { return mBleManager; }
}

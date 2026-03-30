package com.example.watchpusher;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static MainActivity instance;
    private BleManager mBleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        mBleManager = new BleManager(this);

        Button btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v -> {
            // Guide user to enable notification access
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        });

        Button btnConnect = findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(v -> {
            mBleManager.startScanAndConnect();
            Toast.makeText(this, "Scanning for Watch...", Toast.LENGTH_SHORT).show();
        });
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public BleManager getBleManager() {
        return mBleManager;
    }
}

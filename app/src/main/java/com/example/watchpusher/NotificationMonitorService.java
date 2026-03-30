package com.example.watchpusher;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationMonitorService extends NotificationListenerService {
    private static final String TAG = "WatchNotifService";
    private static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    private BleManager mBleManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mBleManager = ((MainActivity) MainActivity.getInstance()).getBleManager();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        Log.d(TAG, "Notification from: " + packageName);

        if (WECHAT_PACKAGENAME.equals(packageName)) {
            Log.i(TAG, "WeChat notification detected! Pushing to Watch...");
            if (mBleManager != null) {
                // Simplified Protocol: Just trigger WX notice
                mBleManager.sendMessage("WX:NewMsg");
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Optional: Reset count if needed
    }
}

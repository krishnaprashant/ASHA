package com.voicenotifier.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.voicenotifier.services.BatteryMonitorService;

/**
 * Broadcast receiver that starts services when device boots
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            Log.d(TAG, "Boot completed, starting services");
            
            try {
                // Start Battery Monitor Service
                Intent serviceIntent = new Intent(context, BatteryMonitorService.class);
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
                
                Log.d(TAG, "Battery Monitor Service started successfully");
                
                // Note: VoiceNotificationService doesn't need to be started here
                // It's automatically bound when notification access is granted
                
            } catch (Exception e) {
                Log.e(TAG, "Error starting services: " + e.getMessage());
            }
        }
    }
}

package com.voicenotifier.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.voicenotifier.MainActivity;
import com.voicenotifier.R;
import com.voicenotifier.tts.VoiceTTSManager;

/**
 * Foreground service that monitors battery level and charging status
 * Speaks alerts when battery is low or charging status changes
 */
public class BatteryMonitorService extends Service {
    private static final String TAG = "BatteryMonitorService";
    private static final String CHANNEL_ID = "battery_monitor_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final int LOW_BATTERY_THRESHOLD = 40;
    
    private VoiceTTSManager ttsManager;
    private BroadcastReceiver batteryReceiver;
    private int lastBatteryLevel = -1;
    private boolean wasCharging = false;
    private boolean hasSpokenLowBatteryAlert = false;
    private boolean hasSpokenBatteryHealthAlert = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        
        // Initialize TTS Manager
        ttsManager = VoiceTTSManager.getInstance(this);
        
        // Create notification channel
        createNotificationChannel();
        
        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Register battery receiver
        registerBatteryReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_STICKY; // Restart if killed by system
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }

    /**
     * Create notification channel for Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Battery Monitor Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitors battery level and charging status");
            channel.setShowBadge(false);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Create persistent notification for foreground service
     */
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Voice Notifier Running")
            .setContentText("Monitoring battery and notifications")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    /**
     * Register broadcast receiver for battery changes
     */
    private void registerBatteryReceiver() {
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    handleBatteryChange(intent);
                }
            }
        };

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
        Log.d(TAG, "Battery receiver registered");
    }

    /**
     * Handle battery change events
     */
    private void handleBatteryChange(Intent intent) {
        // Get battery level
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = (int) ((level / (float) scale) * 100);

        // Get charging status
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                           status == BatteryManager.BATTERY_STATUS_FULL;

        // Get battery health
        int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);

        Log.d(TAG, "Battery: " + batteryPct + "%, Charging: " + isCharging + ", Health: " + health);

        // Check battery health
        checkBatteryHealth(health);

        // Check for low battery alert
        if (batteryPct < LOW_BATTERY_THRESHOLD && !isCharging) {
            if (!hasSpokenLowBatteryAlert) {
                speakBatteryAlert(batteryPct);
                hasSpokenLowBatteryAlert = true;
            }
        } else if (batteryPct >= LOW_BATTERY_THRESHOLD) {
            // Reset flag when battery is above threshold
            hasSpokenLowBatteryAlert = false;
        }

        // Check for charging status change
        if (lastBatteryLevel != -1) {
            if (isCharging && !wasCharging) {
                speakChargingStarted();
            } else if (!isCharging && wasCharging) {
                speakChargingStopped();
            }
        }

        lastBatteryLevel = batteryPct;
        wasCharging = isCharging;
    }

    /**
     * Speak low battery alert
     */
    private void speakBatteryAlert(int batteryLevel) {
        String message = "बैटरी " + LOW_BATTERY_THRESHOLD + " प्रतिशत से कम है। " +
                        "वर्तमान स्तर " + batteryLevel + " प्रतिशत है।";
        ttsManager.speak(message);
        Log.d(TAG, "Low battery alert spoken: " + batteryLevel + "%");
    }

    /**
     * Speak charging started message
     */
    private void speakChargingStarted() {
        ttsManager.speak("चार्जिंग शुरू हो गई है।");
        Log.d(TAG, "Charging started alert spoken");
    }

    /**
     * Speak charging stopped message
     */
    private void speakChargingStopped() {
        ttsManager.speak("चार्जिंग बंद हो गई है।");
        Log.d(TAG, "Charging stopped alert spoken");
    }

    /**
     * Check battery health and alert if poor
     */
    private void checkBatteryHealth(int health) {
        String healthStatus = null;
        
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_DEAD:
                healthStatus = "बैटरी खराब हो गई है।"; // Battery is dead
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                healthStatus = "बैटरी अधिक गर्म हो रही है। कृपया डिवाइस को ठंडा करें।"; // Battery is overheating. Please cool the device.
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                healthStatus = "बैटरी में अधिक वोल्टेज है।"; // Battery has over voltage
                break;
            case BatteryManager.BATTERY_HEALTH_COLD:
                healthStatus = "बैटरी बहुत ठंडी है।"; // Battery is too cold
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                // Battery is good, reset the flag
                hasSpokenBatteryHealthAlert = false;
                return;
            default:
                // Unknown or unspecified, don't alert
                return;
        }
        
        // Speak the health alert only once until it becomes good again
        if (healthStatus != null && !hasSpokenBatteryHealthAlert) {
            ttsManager.speak(healthStatus);
            hasSpokenBatteryHealthAlert = true;
            Log.d(TAG, "Battery health alert spoken: " + health);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        
        // Unregister battery receiver
        if (batteryReceiver != null) {
            try {
                unregisterReceiver(batteryReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
            }
        }
    }
}

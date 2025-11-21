package com.voicenotifier.modules;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.voicenotifier.services.BatteryMonitorService;
import com.voicenotifier.services.VoiceNotificationService;
import com.voicenotifier.tts.VoiceTTSManager;

/**
 * React Native module to control Voice Notifier services
 */
public class VoiceServiceModule extends ReactContextBaseJavaModule {
    private static final String TAG = "VoiceServiceModule";
    private final ReactApplicationContext reactContext;

    public VoiceServiceModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return "VoiceServiceModule";
    }

    /**
     * Start the battery monitor service
     */
    @ReactMethod
    public void startBatteryMonitor(Promise promise) {
        try {
            Intent serviceIntent = new Intent(reactContext, BatteryMonitorService.class);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                reactContext.startForegroundService(serviceIntent);
            } else {
                reactContext.startService(serviceIntent);
            }
            
            Log.d(TAG, "Battery Monitor Service started");
            promise.resolve("Battery monitor started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error starting battery monitor: " + e.getMessage());
            promise.reject("START_ERROR", e.getMessage());
        }
    }

    /**
     * Stop the battery monitor service
     */
    @ReactMethod
    public void stopBatteryMonitor(Promise promise) {
        try {
            Intent serviceIntent = new Intent(reactContext, BatteryMonitorService.class);
            reactContext.stopService(serviceIntent);
            
            Log.d(TAG, "Battery Monitor Service stopped");
            promise.resolve("Battery monitor stopped successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping battery monitor: " + e.getMessage());
            promise.reject("STOP_ERROR", e.getMessage());
        }
    }

    /**
     * Check if notification listener permission is granted
     */
    @ReactMethod
    public void isNotificationListenerEnabled(Promise promise) {
        try {
            String packageName = reactContext.getPackageName();
            String flat = Settings.Secure.getString(
                reactContext.getContentResolver(),
                "enabled_notification_listeners"
            );
            
            if (flat != null && flat.contains(packageName)) {
                promise.resolve(true);
            } else {
                promise.resolve(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking notification listener: " + e.getMessage());
            promise.reject("CHECK_ERROR", e.getMessage());
        }
    }

    /**
     * Open notification listener settings
     */
    @ReactMethod
    public void openNotificationSettings(Promise promise) {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            reactContext.startActivity(intent);
            promise.resolve("Opened notification settings");
        } catch (Exception e) {
            Log.e(TAG, "Error opening settings: " + e.getMessage());
            promise.reject("SETTINGS_ERROR", e.getMessage());
        }
    }

    /**
     * Open battery optimization settings
     */
    @ReactMethod
    public void openBatterySettings(Promise promise) {
        try {
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            } else {
                intent = new Intent(Settings.ACTION_SETTINGS);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            reactContext.startActivity(intent);
            promise.resolve("Opened battery settings");
        } catch (Exception e) {
            Log.e(TAG, "Error opening battery settings: " + e.getMessage());
            promise.reject("SETTINGS_ERROR", e.getMessage());
        }
    }

    /**
     * Start all services
     */
    @ReactMethod
    public void startAllServices(Promise promise) {
        try {
            // Start battery monitor
            Intent serviceIntent = new Intent(reactContext, BatteryMonitorService.class);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                reactContext.startForegroundService(serviceIntent);
            } else {
                reactContext.startService(serviceIntent);
            }
            
            Log.d(TAG, "All services started");
            promise.resolve("All services started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error starting services: " + e.getMessage());
            promise.reject("START_ERROR", e.getMessage());
        }
    }

    /**
     * Stop all services
     */
    @ReactMethod
    public void stopAllServices(Promise promise) {
        try {
            // Stop battery monitor
            Intent serviceIntent = new Intent(reactContext, BatteryMonitorService.class);
            reactContext.stopService(serviceIntent);
            
            Log.d(TAG, "All services stopped");
            promise.resolve("All services stopped successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping services: " + e.getMessage());
            promise.reject("STOP_ERROR", e.getMessage());
        }
    }

    /**
     * Test voice output with a sample message
     */
    @ReactMethod
    public void testVoice(Promise promise) {
        try {
            VoiceTTSManager ttsManager = VoiceTTSManager.getInstance(reactContext);
            ttsManager.speak("यह एक परीक्षण संदेश है। आशा आपकी सेवा में है।");
            
            Log.d(TAG, "Test voice played");
            promise.resolve("Test voice played successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error testing voice: " + e.getMessage());
            promise.reject("TEST_ERROR", e.getMessage());
        }
    }
    
    /**
     * Get all available Hindi voices
     */
    @ReactMethod
    public void getAvailableVoices(Promise promise) {
        try {
            VoiceTTSManager ttsManager = VoiceTTSManager.getInstance(reactContext);
            java.util.List<String> voices = ttsManager.getAvailableHindiVoices();
            
            WritableArray voiceArray = Arguments.createArray();
            for (String voiceName : voices) {
                voiceArray.pushString(voiceName);
            }
            
            promise.resolve(voiceArray);
        } catch (Exception e) {
            Log.e(TAG, "Error getting voices: " + e.getMessage());
            promise.reject("GET_VOICES_ERROR", e.getMessage());
        }
    }
    
    /**
     * Test a specific voice by name
     */
    @ReactMethod
    public void testVoiceByName(String voiceName, Promise promise) {
        try {
            VoiceTTSManager ttsManager = VoiceTTSManager.getInstance(reactContext);
            boolean success = ttsManager.setVoiceByName(voiceName);
            
            if (success) {
                ttsManager.speak("यह " + voiceName + " आवाज़ है।");
                promise.resolve("Testing voice: " + voiceName);
            } else {
                promise.reject("VOICE_NOT_FOUND", "Voice not found: " + voiceName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error testing voice: " + e.getMessage());
            promise.reject("TEST_VOICE_ERROR", e.getMessage());
        }
    }
}

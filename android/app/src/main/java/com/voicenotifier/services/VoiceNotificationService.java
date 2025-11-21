package com.voicenotifier.services;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.voicenotifier.tts.VoiceTTSManager;

/**
 * Notification Listener Service that reads notifications from specific apps
 * and speaks them aloud using TTS
 */
public class VoiceNotificationService extends NotificationListenerService {
    private static final String TAG = "VoiceNotificationService";
    private VoiceTTSManager ttsManager;
    
    // Package names for supported apps
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static final String TELEGRAM_PACKAGE = "org.telegram.messenger";
    private static final String MESSAGES_PACKAGE = "com.google.android.apps.messaging";
    private static final String SAMSUNG_MESSAGES = "com.samsung.android.messaging";
    private static final String DEFAULT_SMS = "com.android.messaging";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Voice Notification Service created");
        ttsManager = VoiceTTSManager.getInstance(this);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String packageName = sbn.getPackageName();
            Notification notification = sbn.getNotification();
            
            // Check if notification is from supported apps
            if (!isSupportedApp(packageName)) {
                return;
            }

            // Skip silent notifications
            if ((notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) {
                Log.d(TAG, "Skipping ongoing notification");
                return;
            }

            // Extract notification details
            Bundle extras = notification.extras;
            String title = extras.getString(Notification.EXTRA_TITLE);
            String text = extras.getCharSequence(Notification.EXTRA_TEXT) != null ?
                         extras.getCharSequence(Notification.EXTRA_TEXT).toString() : "";
            
            // Skip if no content
            if (text == null || text.trim().isEmpty()) {
                Log.d(TAG, "Skipping empty notification");
                return;
            }

            // Skip group summary notifications
            if ((notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0) {
                Log.d(TAG, "Skipping group summary notification");
                return;
            }

            // Get app name
            String appName = getAppName(packageName);
            
            // Build message (for future API integration)
            String messageToSpeak = buildMessage(appName, title, text);
            
            // Log the notification (will be sent to API in future)
            if (messageToSpeak != null && !messageToSpeak.isEmpty()) {
                Log.d(TAG, "Notification received: " + messageToSpeak);
                // TODO: Send to API endpoint in future
                // ttsManager.speakWithDelay(messageToSpeak, 500); // Disabled - keeping silent
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing notification: " + e.getMessage());
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Not needed for this use case
    }

    /**
     * Check if the app is supported for voice notifications
     */
    private boolean isSupportedApp(String packageName) {
        return packageName.equals(WHATSAPP_PACKAGE) ||
               packageName.equals(TELEGRAM_PACKAGE) ||
               packageName.equals(MESSAGES_PACKAGE) ||
               packageName.equals(SAMSUNG_MESSAGES) ||
               packageName.equals(DEFAULT_SMS);
    }

    /**
     * Get readable app name from package name
     */
    private String getAppName(String packageName) {
        switch (packageName) {
            case WHATSAPP_PACKAGE:
                return "WhatsApp";
            case TELEGRAM_PACKAGE:
                return "Telegram";
            case MESSAGES_PACKAGE:
            case SAMSUNG_MESSAGES:
            case DEFAULT_SMS:
                return "Messages";
            default:
                return "App";
        }
    }

    /**
     * Build the message to speak
     */
    private String buildMessage(String appName, String title, String text) {
        try {
            // Clean up the text
            String cleanedText = cleanText(text);
            
            // Skip if text is too short or just emoji
            if (cleanedText.length() < 2) {
                return null;
            }

            // Build message format: "WhatsApp message from John: Hello there"
            StringBuilder message = new StringBuilder();
            message.append(appName).append(" message");
            
            if (title != null && !title.trim().isEmpty() && !title.equals(appName)) {
                // Clean the title (sender name)
                String cleanedTitle = cleanText(title);
                if (!cleanedTitle.isEmpty()) {
                    message.append(" from ").append(cleanedTitle);
                }
            }
            
            message.append(": ").append(cleanedText);
            
            // Limit message length to avoid very long messages
            String finalMessage = message.toString();
            if (finalMessage.length() > 200) {
                finalMessage = finalMessage.substring(0, 197) + "...";
            }
            
            return finalMessage;
            
        } catch (Exception e) {
            Log.e(TAG, "Error building message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Clean text for better TTS readability
     */
    private String cleanText(String text) {
        if (text == null) return "";
        
        // Remove excessive whitespace
        text = text.trim().replaceAll("\\s+", " ");
        
        // Remove URLs (they don't sound good when spoken)
        text = text.replaceAll("https?://\\S+", "link");
        
        // Replace common symbols with words
        text = text.replace("@", "at");
        text = text.replace("&", "and");
        text = text.replace("#", "hashtag");
        
        return text;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "Notification listener connected");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "Notification listener disconnected");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Voice Notification Service destroyed");
    }
}

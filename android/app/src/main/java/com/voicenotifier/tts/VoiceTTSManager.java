package com.voicenotifier.tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.util.Locale;

/**
 * Singleton class for managing Text-to-Speech functionality
 * Shared across all services to avoid multiple TTS initializations
 */
public class VoiceTTSManager {
    private static final String TAG = "VoiceTTSManager";
    private static VoiceTTSManager instance;
    private TextToSpeech tts;
    private boolean isInitialized = false;
    private Context context;

    private VoiceTTSManager(Context context) {
        this.context = context.getApplicationContext();
        initializeTTS();
    }

    /**
     * Get singleton instance of VoiceTTSManager
     */
    public static synchronized VoiceTTSManager getInstance(Context context) {
        if (instance == null) {
            instance = new VoiceTTSManager(context);
        }
        return instance;
    }

    /**
     * Initialize Text-to-Speech engine
     */
    private void initializeTTS() {
        try {
            tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = tts.setLanguage(Locale.US);
                        if (result == TextToSpeech.LANG_MISSING_DATA || 
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e(TAG, "Language not supported");
                        } else {
                            // Set speech parameters
                            tts.setSpeechRate(1.0f);  // Normal speed
                            tts.setPitch(1.0f);       // Normal pitch
                            isInitialized = true;
                            Log.d(TAG, "TTS initialized successfully");
                        }
                    } else {
                        Log.e(TAG, "TTS initialization failed");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing TTS: " + e.getMessage());
        }
    }

    /**
     * Speak text using TTS
     * @param text The text to speak
     */
    public void speak(String text) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized, reinitializing...");
            initializeTTS();
            return;
        }

        try {
            // Stop any current speech
            if (tts.isSpeaking()) {
                tts.stop();
            }
            
            // Speak the new text
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            Log.d(TAG, "Speaking: " + text);
        } catch (Exception e) {
            Log.e(TAG, "Error speaking text: " + e.getMessage());
        }
    }

    /**
     * Speak text after a delay
     * @param text The text to speak
     * @param delayMs Delay in milliseconds
     */
    public void speakWithDelay(final String text, long delayMs) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                speak(text);
            }
        }, delayMs);
    }

    /**
     * Stop speaking
     */
    public void stop() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
    }

    /**
     * Check if TTS is currently speaking
     */
    public boolean isSpeaking() {
        return tts != null && tts.isSpeaking();
    }

    /**
     * Shutdown TTS engine
     * Call this when the app is destroyed
     */
    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            isInitialized = false;
            Log.d(TAG, "TTS shutdown");
        }
    }

    /**
     * Set speech rate
     * @param rate Speech rate (1.0 is normal)
     */
    public void setSpeechRate(float rate) {
        if (tts != null) {
            tts.setSpeechRate(rate);
        }
    }

    /**
     * Set pitch
     * @param pitch Speech pitch (1.0 is normal)
     */
    public void setPitch(float pitch) {
        if (tts != null) {
            tts.setPitch(pitch);
        }
    }
}

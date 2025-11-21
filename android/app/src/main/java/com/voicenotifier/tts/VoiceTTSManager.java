package com.voicenotifier.tts;

import android.content.Context;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import java.util.Locale;
import java.util.Set;

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
            // Use Google's TTS engine explicitly (com.google.android.tts)
            String googleTTSEngine = "com.google.android.tts";
            
            Log.d(TAG, "Initializing TTS with Google engine: " + googleTTSEngine);
            
            tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        // Log which engine is actually being used
                        String currentEngine = tts.getDefaultEngine();
                        Log.d(TAG, "TTS engine in use: " + currentEngine);
                        
                        // Try Hindi (India) first
                        Locale hindiIndia = new Locale("hi", "IN");
                        int result = tts.setLanguage(hindiIndia);
                        
                        // If Hindi not available, fallback to English (India)
                        if (result == TextToSpeech.LANG_MISSING_DATA || 
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.w(TAG, "Hindi not supported, trying English (India)");
                            result = tts.setLanguage(new Locale("en", "IN"));
                            
                            // Last fallback to US English
                            if (result == TextToSpeech.LANG_MISSING_DATA || 
                                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.w(TAG, "English (India) not supported, using US English");
                                tts.setLanguage(Locale.US);
                            }
                        }
                        
                        Log.d(TAG, "Language set to: " + tts.getLanguage());
                        
                        // Try to set a male voice with deep, resonant characteristics (Jarvis-like)
                        setMaleVoice();
                        
                        // Apply slight adjustments: slightly higher pitch, slightly slower speed
                        tts.setPitch(1.05f);      // Very slightly higher pitch (5% increase)
                        tts.setSpeechRate(0.92f); // Slightly slower (8% reduction)
                        Log.d(TAG, "Applied custom TTS settings - Rate: 0.92, Pitch: 1.05");
                        
                        isInitialized = true;
                        Log.d(TAG, "TTS initialized successfully");
                    } else {
                        Log.e(TAG, "TTS initialization failed with status: " + status);
                    }
                }
            }, googleTTSEngine);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing TTS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Apply system TTS settings for speech rate and pitch
     */
    private void applySystemTTSSettings() {
        try {
            // Read system TTS settings
            int speechRate = Settings.Secure.getInt(
                context.getContentResolver(),
                Settings.Secure.TTS_DEFAULT_RATE,
                100  // Default value if not set
            );
            
            int pitch = Settings.Secure.getInt(
                context.getContentResolver(),
                Settings.Secure.TTS_DEFAULT_PITCH,
                100  // Default value if not set
            );
            
            Log.d(TAG, "System TTS settings (raw) - Rate: " + speechRate + ", Pitch: " + pitch);
            
            // Convert system settings (0-600, default 100) to TTS values (0.0-6.0, default 1.0)
            float rateFloat = speechRate / 100.0f;
            float pitchFloat = pitch / 100.0f;
            
            // Clamp values to reasonable ranges
            rateFloat = Math.max(0.1f, Math.min(rateFloat, 4.0f));
            pitchFloat = Math.max(0.1f, Math.min(pitchFloat, 4.0f));
            
            tts.setSpeechRate(rateFloat);
            tts.setPitch(pitchFloat);
            
            Log.d(TAG, "Applied TTS settings - Rate: " + rateFloat + ", Pitch: " + pitchFloat);
            
            // Also log what the TTS engine reports
            Log.d(TAG, "Current TTS engine: " + tts.getDefaultEngine());
        } catch (Exception e) {
            Log.w(TAG, "Error reading system TTS settings, using defaults: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set male voice if available - specifically Voice III for Hindi
     */
    private void setMaleVoice() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            try {
                Set<Voice> voices = tts.getVoices();
                if (voices != null) {
                    Voice selectedVoice = null;
                    Voice voiceIV = null;  // hid - typically male
                    Voice voiceIII = null; // hic
                    Voice voiceII = null;  // hib
                    Voice voiceI = null;   // hia
                    Voice maleVoice = null;
                    
                    Log.d(TAG, "Available voices:");
                    for (Voice voice : voices) {
                        Locale voiceLocale = voice.getLocale();
                        String name = voice.getName();
                        
                        if (voiceLocale != null && voiceLocale.getLanguage().equals("hi")) {
                            Log.d(TAG, "  Hindi voice: " + name + " (Quality: " + voice.getQuality() + ")");
                            
                            // Check for male gender in features
                            if (voice.getFeatures() != null) {
                                for (String feature : voice.getFeatures()) {
                                    if (feature.toLowerCase().contains("male") && !feature.toLowerCase().contains("female")) {
                                        maleVoice = voice;
                                        Log.d(TAG, "  ✓✓✓ FOUND MALE VOICE: " + name);
                                    }
                                }
                            }
                            
                            // Categorize voices by type
                            // Voice I = hia (female), Voice II = hib (female), Voice III = hic (female), Voice IV = hid (male)
                            if (name.toLowerCase().contains("hid")) {
                                voiceIV = voice;
                                Log.d(TAG, "  ✓ Found Voice IV (typically male): " + name);
                            } else if (name.toLowerCase().contains("hic")) {
                                voiceIII = voice;
                                Log.d(TAG, "  ✓ Found Voice III: " + name);
                            } else if (name.toLowerCase().contains("hib")) {
                                voiceII = voice;
                            } else if (name.toLowerCase().contains("hia")) {
                                voiceI = voice;
                            }
                            
                            // Keep track of any Hindi voice as fallback
                            if (selectedVoice == null) {
                                selectedVoice = voice;
                            }
                        }
                    }
                    
                    // Priority: Male voice > Voice IV (hid) > Voice III (hic) > Any Hindi voice
                    if (maleVoice != null) {
                        selectedVoice = maleVoice;
                        Log.d(TAG, "Using MALE Hindi voice: " + selectedVoice.getName());
                    } else if (voiceIV != null) {
                        selectedVoice = voiceIV;
                        Log.d(TAG, "Using Hindi Voice IV (likely male): " + selectedVoice.getName());
                    } else if (voiceIII != null) {
                        selectedVoice = voiceIII;
                        Log.d(TAG, "Using Hindi Voice III: " + selectedVoice.getName());
                    }
                    
                    if (selectedVoice != null) {
                        tts.setVoice(selectedVoice);
                        Log.d(TAG, "Voice set successfully: " + selectedVoice.getName());
                    } else {
                        Log.w(TAG, "No Hindi voice found, using default");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting voice: " + e.getMessage());
                e.printStackTrace();
            }
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
    
    /**
     * Get list of all available Hindi voice names
     */
    public java.util.List<String> getAvailableHindiVoices() {
        java.util.List<String> voiceNames = new java.util.ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && tts != null) {
            try {
                Set<Voice> voices = tts.getVoices();
                if (voices != null) {
                    for (Voice voice : voices) {
                        Locale voiceLocale = voice.getLocale();
                        if (voiceLocale != null && voiceLocale.getLanguage().equals("hi")) {
                            voiceNames.add(voice.getName());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting voices: " + e.getMessage());
            }
        }
        return voiceNames;
    }
    
    /**
     * Set voice by name
     * @param voiceName The name of the voice to use
     * @return true if successful
     */
    public boolean setVoiceByName(String voiceName) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && tts != null) {
            try {
                Set<Voice> voices = tts.getVoices();
                if (voices != null) {
                    for (Voice voice : voices) {
                        if (voice.getName().equals(voiceName)) {
                            tts.setVoice(voice);
                            Log.d(TAG, "Voice set to: " + voiceName);
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting voice: " + e.getMessage());
            }
        }
        return false;
    }
}

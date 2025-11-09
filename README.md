# 🔊 Voice Notifier

A personal voice notification app for Android that runs silently in the background and **speaks out important messages and battery alerts** using Android's Text-to-Speech (TTS) engine.

## 🎯 Features

- ✅ **Battery Monitoring** - Speaks alerts when battery drops below 40%
- ✅ **Notification Reading** - Reads WhatsApp, Telegram, and SMS messages aloud
- ✅ **Background Service** - Runs 24/7 without user interaction
- ✅ **Auto-Start on Boot** - Automatically starts when device boots
- ✅ **Offline & Local** - No internet required, completely private
- ✅ **No UI Required** - Works entirely in the background

## 📱 How It Works

Once installed and configured:

1. **Battery Alerts**: When your battery level drops below 40%, the app speaks:
   > "Battery is less than 40 percent. Current level is [X] percent."

2. **Charging Status**: When charging starts or stops:
   > "Charging started." / "Charging stopped."

3. **Message Notifications**: When you receive a WhatsApp, Telegram, or SMS message:
   > "WhatsApp message from John: Hello there."

## 🚀 Installation & Setup

### Prerequisites

- Node.js (v18 or higher)
- Android Studio
- JDK 17
- Android device (Android 7.0+ / API 24+)

### Step 1: Install Dependencies

```bash
cd ASHA
npm install
```

### Step 2: Build the App

```bash
# Connect your Android device via USB or start an emulator
npx react-native run-android
```

### Step 3: Grant Permissions

After installing, you need to grant essential permissions:

#### 🔔 Notification Access
1. Open the app
2. Tap **"Notification Access"**
3. Enable **Voice Notifier** in the system settings
4. Go back to the app

#### 🔋 Battery Optimization
1. Tap **"Battery Optimization"**
2. Find **Voice Notifier** in the list
3. Select **"Don't optimize"** or **"Allow"**
4. This ensures the service runs continuously

### Step 4: Start the Service

1. In the app, tap **"Start Service"**
2. You'll see a persistent notification: "Voice Notifier Running"
3. The service is now active!

## 🔧 Configuration

### Supported Apps

The notification listener currently reads messages from:
- **WhatsApp** (`com.whatsapp`)
- **Telegram** (`org.telegram.messenger`)
- **Messages** (Google, Samsung, default SMS apps)

To add more apps, edit:
```
android/app/src/main/java/com/voicenotifier/services/VoiceNotificationService.java
```

### Low Battery Threshold

Default threshold is **40%**. To change it, edit:
```java
// android/app/src/main/java/com/voicenotifier/services/BatteryMonitorService.java
private static final int LOW_BATTERY_THRESHOLD = 40; // Change this value
```

### TTS Settings

Speech rate and pitch are set to **1.0** (normal). To customize:
```java
// android/app/src/main/java/com/voicenotifier/tts/VoiceTTSManager.java
tts.setSpeechRate(1.0f);  // Speed (0.5 = slow, 2.0 = fast)
tts.setPitch(1.0f);       // Pitch (0.5 = low, 2.0 = high)
```

## 🛠️ Architecture

### Native Android Components

```
android/app/src/main/java/com/voicenotifier/
├── tts/
│   └── VoiceTTSManager.java          # Singleton TTS manager
├── services/
│   ├── BatteryMonitorService.java    # Battery monitoring foreground service
│   └── VoiceNotificationService.java # Notification listener service
├── receivers/
│   └── BootReceiver.java             # Boot broadcast receiver
└── modules/
    ├── VoiceServiceModule.java       # React Native bridge
    └── VoiceServicePackage.java      # Module package registration
```

### Key Services

1. **BatteryMonitorService**
   - Runs as a foreground service
   - Listens to `ACTION_BATTERY_CHANGED` broadcasts
   - Speaks battery alerts when level drops below threshold
   - Announces charging status changes

2. **VoiceNotificationService**
   - Extends `NotificationListenerService`
   - Filters notifications from supported apps
   - Extracts sender name and message text
   - Speaks notifications using TTS

3. **VoiceTTSManager**
   - Singleton pattern for shared TTS instance
   - Manages TTS lifecycle
   - Provides simple speak/stop interface

## 📝 Permissions Explained

| Permission | Purpose |
|------------|---------|
| `FOREGROUND_SERVICE` | Run services in the background |
| `RECEIVE_BOOT_COMPLETED` | Auto-start on device boot |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Read notifications |
| `BATTERY_STATS` | Monitor battery level |
| `WAKE_LOCK` | Keep CPU awake when speaking |
| `POST_NOTIFICATIONS` | Show foreground service notification |

## 🐛 Troubleshooting

### Service Not Starting
- Check if battery optimization is disabled
- Ensure notification access is granted
- Restart the device

### TTS Not Working
- Go to Android Settings → Accessibility → Text-to-Speech
- Ensure a TTS engine is installed (Google TTS is recommended)
- Test TTS output in system settings

### Notifications Not Being Read
- Verify notification access is granted
- Check if the app package name matches the configured apps
- Look at logs: `adb logcat | findstr VoiceNotificationService`

### Service Stops After Some Time
- Disable battery optimization for the app
- Check if "Background restrictions" are disabled
- Some manufacturers (Xiaomi, Huawei) have aggressive battery savers - whitelist the app

## 📊 Logs & Debugging

View real-time logs:
```bash
# All logs
adb logcat | findstr VoiceNotifier

# Battery service logs
adb logcat | findstr BatteryMonitorService

# Notification service logs
adb logcat | findstr VoiceNotificationService

# TTS logs
adb logcat | findstr VoiceTTSManager
```

## 🔮 Future Enhancements

Potential features to add:

- ⏰ **Quiet Hours** - Mute notifications between specific times (e.g., 11 PM - 7 AM)
- 🎚️ **Voice Customization** - Adjust speech rate, pitch, and volume
- 📱 **Settings UI** - React Native screen for configuration
- 🌐 **Multi-language Support** - Support for different TTS languages
- 📞 **Call Announcements** - Announce incoming calls
- 🔕 **Smart Filtering** - Skip group messages or certain contacts
- 📈 **Usage Statistics** - Track how many notifications were spoken

## 📄 License

This project is for personal use. Feel free to modify and extend as needed.

## 🤝 Contributing

This is a personal project, but suggestions and improvements are welcome!

## ⚠️ Important Notes

- This app requires **notification access**, which is a sensitive permission. Only grant it to apps you trust.
- Battery monitoring and TTS can consume battery. The impact is minimal but noticeable.
- Some devices may kill background services aggressively. Check manufacturer-specific battery settings.
- The app is designed for personal use and may not scale for commercial applications.

## 📞 Support

For issues or questions:
1. Check the troubleshooting section above
2. Review logs using `adb logcat`
3. Verify all permissions are granted
4. Ensure Android version is 7.0+ (API 24+)

---

**Built with ❤️ using React Native and Native Android**

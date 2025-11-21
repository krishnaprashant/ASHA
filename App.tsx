import React, {useEffect, useState} from 'react';
import {
  SafeAreaView,
  StatusBar,
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  ScrollView,
  Alert,
} from 'react-native';
import {NativeModules} from 'react-native';

const {VoiceServiceModule} = NativeModules;

function App(): React.JSX.Element {
  const [isServiceRunning, setIsServiceRunning] = useState(false);
  const [notificationEnabled, setNotificationEnabled] = useState(false);

  useEffect(() => {
    checkNotificationPermission();
  }, []);

  const checkNotificationPermission = async () => {
    try {
      const enabled = await VoiceServiceModule.isNotificationListenerEnabled();
      setNotificationEnabled(enabled);
    } catch (error) {
      console.error('Error checking notification permission:', error);
    }
  };

  const handleStartService = async () => {
    try {
      await VoiceServiceModule.startAllServices();
      setIsServiceRunning(true);
      Alert.alert('Success', 'ASHA service started!');
    } catch (error) {
      Alert.alert('Error', 'Failed to start service: ' + error);
    }
  };

  const handleStopService = async () => {
    try {
      await VoiceServiceModule.stopAllServices();
      setIsServiceRunning(false);
      Alert.alert('Success', 'ASHA service stopped!');
    } catch (error) {
      Alert.alert('Error', 'Failed to stop service: ' + error);
    }
  };

  const openNotificationSettings = async () => {
    try {
      await VoiceServiceModule.openNotificationSettings();
    } catch (error) {
      Alert.alert('Error', 'Failed to open settings: ' + error);
    }
  };

  const openBatterySettings = async () => {
    try {
      await VoiceServiceModule.openBatterySettings();
    } catch (error) {
      Alert.alert('Error', 'Failed to open settings: ' + error);
    }
  };

  const handleTestVoice = async () => {
    try {
      await VoiceServiceModule.testVoice();
    } catch (error) {
      Alert.alert('Error', 'Failed to test voice: ' + error);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#fff" />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <Text style={styles.title}>🔊 RAVI</Text>
          <Text style={styles.subtitle}>Background Voice Notifications</Text>
        </View>

        <View style={styles.statusCard}>
          <Text style={styles.statusLabel}>Service Status</Text>
          <View style={styles.statusIndicator}>
            <View
              style={[
                styles.statusDot,
                {backgroundColor: isServiceRunning ? '#4CAF50' : '#F44336'},
              ]}
            />
            <Text style={styles.statusText}>
              {isServiceRunning ? 'Running' : 'Stopped'}
            </Text>
          </View>
        </View>

        <View style={styles.statusCard}>
          <Text style={styles.statusLabel}>Notification Access</Text>
          <View style={styles.statusIndicator}>
            <View
              style={[
                styles.statusDot,
                {backgroundColor: notificationEnabled ? '#4CAF50' : '#F44336'},
              ]}
            />
            <Text style={styles.statusText}>
              {notificationEnabled ? 'Granted' : 'Not Granted'}
            </Text>
          </View>
        </View>

        <View style={styles.buttonContainer}>
          <TouchableOpacity
            style={[styles.button, styles.primaryButton]}
            onPress={handleStartService}>
            <Text style={styles.buttonText}>Start Service</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.button, styles.secondaryButton]}
            onPress={handleStopService}>
            <Text style={[styles.buttonText, styles.secondaryButtonText]}>
              Stop Service
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.button, styles.testButton]}
            onPress={handleTestVoice}>
            <Text style={styles.buttonText}>🔊 Test Voice</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.settingsSection}>
          <Text style={styles.sectionTitle}>⚙️ Settings</Text>

          <TouchableOpacity
            style={styles.settingsButton}
            onPress={openNotificationSettings}>
            <Text style={styles.settingsButtonText}>
              📱 Notification Access
            </Text>
            <Text style={styles.settingsButtonSubtext}>
              Enable notification reading
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.settingsButton}
            onPress={openBatterySettings}>
            <Text style={styles.settingsButtonText}>🔋 Battery Optimization</Text>
            <Text style={styles.settingsButtonSubtext}>
              Disable battery optimization
            </Text>
          </TouchableOpacity>
        </View>

        <View style={styles.infoSection}>
          <Text style={styles.infoTitle}>ℹ️ How it works</Text>
          <Text style={styles.infoText}>
            • Speaks battery alerts when level drops below 40%
          </Text>
          <Text style={styles.infoText}>
            • Reads WhatsApp, Telegram, and SMS notifications aloud
          </Text>
          <Text style={styles.infoText}>
            • Runs silently in the background
          </Text>
          <Text style={styles.infoText}>
            • Auto-starts on device boot
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  scrollContent: {
    padding: 20,
  },
  header: {
    alignItems: 'center',
    marginBottom: 30,
    marginTop: 20,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
  },
  statusCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 20,
    marginBottom: 15,
    shadowColor: '#000',
    shadowOffset: {width: 0, height: 2},
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  statusLabel: {
    fontSize: 14,
    color: '#666',
    marginBottom: 10,
  },
  statusIndicator: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statusDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
    marginRight: 10,
  },
  statusText: {
    fontSize: 18,
    fontWeight: '600',
    color: '#333',
  },
  buttonContainer: {
    marginVertical: 20,
  },
  button: {
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
    marginBottom: 12,
  },
  primaryButton: {
    backgroundColor: '#2196F3',
  },
  secondaryButton: {
    backgroundColor: '#fff',
    borderWidth: 2,
    borderColor: '#2196F3',
  },
  buttonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#fff',
  },
  secondaryButtonText: {
    color: '#2196F3',
  },
  testButton: {
    backgroundColor: '#FF9800',
  },
  settingsSection: {
    marginTop: 20,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 15,
  },
  settingsButton: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: {width: 0, height: 2},
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  settingsButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 4,
  },
  settingsButtonSubtext: {
    fontSize: 13,
    color: '#666',
  },
  infoSection: {
    marginTop: 30,
    backgroundColor: '#E3F2FD',
    borderRadius: 12,
    padding: 16,
  },
  infoTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 12,
  },
  infoText: {
    fontSize: 14,
    color: '#555',
    marginBottom: 8,
    lineHeight: 20,
  },
});

export default App;

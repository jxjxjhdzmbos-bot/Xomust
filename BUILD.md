# Build Instructions for Xomust Android Game

## Prerequisites

### Required Software
1. **Android Studio** (Arctic Fox or later recommended)
   - Download from: https://developer.android.com/studio
   
2. **Java Development Kit (JDK)**
   - JDK 8 or later (JDK 11 recommended)
   - Usually bundled with Android Studio
   
3. **Android SDK**
   - Automatically installed with Android Studio
   - Minimum SDK 21 (Android 5.0)
   - Target SDK 34 (Android 14)

### Firebase Configuration

#### Step 1: Create Firebase Project
1. Go to https://console.firebase.google.com/
2. Click "Add project" or select an existing project
3. Follow the setup wizard

#### Step 2: Add Android App to Firebase
1. In Firebase Console, click "Add app" and select Android
2. Register app with package name: `com.xomust.game`
3. Download the `google-services.json` file
4. Replace `app/google-services.json` with your downloaded file

#### Step 3: Enable Firebase Services

**Authentication:**
1. Navigate to Authentication > Sign-in method
2. Enable "Google" provider
3. Add your SHA-1 certificate fingerprint:
   ```bash
   # For debug builds
   keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android -keypass android
   ```
4. Save the configuration

**Realtime Database:**
1. Navigate to Realtime Database
2. Click "Create Database"
3. Start in **test mode** (for development)
4. Note: For production, configure proper security rules

**Storage:**
1. Navigate to Storage
2. Click "Get Started"
3. Start in **test mode** (for development)
4. Note: For production, configure proper security rules

#### Step 4: Configure Google Sign-In
1. In Firebase Console > Authentication > Sign-in method > Google
2. Copy the "Web client ID"
3. Open `app/src/main/res/values/strings.xml`
4. Replace `YOUR_WEB_CLIENT_ID_HERE` with your actual Web client ID:
   ```xml
   <string name="default_web_client_id">YOUR_ACTUAL_WEB_CLIENT_ID_HERE</string>
   ```

## Building the Project

### Option 1: Using Android Studio (Recommended)

1. **Open Project:**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the Xomust directory and click "OK"

2. **Sync Gradle:**
   - Android Studio should automatically sync Gradle
   - If not, click "File" > "Sync Project with Gradle Files"

3. **Build:**
   - Select "Build" > "Make Project" (or press Ctrl+F9 / Cmd+F9)
   - Wait for the build to complete

4. **Run:**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green play icon) or press Shift+F10
   - Select your device/emulator

### Option 2: Using Command Line

1. **Set ANDROID_HOME:**
   ```bash
   # Linux/Mac
   export ANDROID_HOME=$HOME/Android/Sdk
   export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
   
   # Windows
   set ANDROID_HOME=C:\Users\YourUsername\AppData\Local\Android\Sdk
   set PATH=%PATH%;%ANDROID_HOME%\tools;%ANDROID_HOME%\platform-tools
   ```

2. **Build Debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```
   
   The APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

3. **Install on Device:**
   ```bash
   # Make sure device is connected and USB debugging is enabled
   ./gradlew installDebug
   ```

4. **Build Release APK:**
   ```bash
   ./gradlew assembleRelease
   ```
   Note: Release builds require signing configuration

### Option 3: Using Gradle Wrapper Only

If you only have JDK installed (no Android Studio):

1. Download Android SDK command-line tools from:
   https://developer.android.com/studio#command-tools

2. Extract and set up the SDK:
   ```bash
   cd /path/to/cmdline-tools
   ./bin/sdkmanager --sdk_root=$HOME/Android/Sdk "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```

3. Set ANDROID_HOME and build as described in Option 2

## Troubleshooting

### Build Errors

**"SDK location not found":**
- Create `local.properties` in project root:
  ```properties
  sdk.dir=/path/to/Android/Sdk
  ```

**Firebase/Google Services errors:**
- Ensure `google-services.json` is properly configured
- Verify all Firebase services are enabled
- Check that package name matches: `com.xomust.game`

**Google Sign-In not working:**
- Verify SHA-1 fingerprint is added in Firebase Console
- Ensure Web client ID is correctly set in strings.xml
- Check that Google Sign-In is enabled in Firebase Authentication

**Gradle sync failed:**
- Check internet connection
- Clear Gradle cache: `./gradlew clean --refresh-dependencies`
- Invalidate caches in Android Studio: File > Invalidate Caches / Restart

### Runtime Errors

**App crashes on startup:**
- Check if `google-services.json` is configured
- Verify Firebase project is active
- Check logcat for specific error messages

**Google Sign-In fails:**
- Ensure device has Google Play Services
- Check internet connection
- Verify Firebase configuration is correct

**Online mode doesn't work:**
- Check Realtime Database rules
- Ensure internet connection is active
- Verify Firebase Realtime Database is enabled

## Testing

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] Google Sign-In works
- [ ] Username and profile image can be set
- [ ] Offline PvP mode works
- [ ] Offline PvAI mode works (all difficulty levels)
- [ ] Online room creation works
- [ ] Online room joining works
- [ ] Real-time gameplay synchronization works
- [ ] Disconnect handling works
- [ ] About screen displays correctly

## Security Notes

⚠️ **Important for Production:**

1. **Firebase Security Rules:**
   - Do NOT use test mode in production
   - Configure proper authentication rules for Realtime Database
   - Configure proper authentication rules for Storage

2. **API Keys:**
   - The Web client ID is safe to include in the app
   - Keep your Firebase service account keys secure
   - Don't commit real `google-services.json` to public repositories if it contains sensitive data

3. **ProGuard/R8:**
   - Enable minification for release builds
   - Configure ProGuard rules for Firebase and Google Play Services
   - Test release builds thoroughly

## Additional Resources

- [Android Developer Documentation](https://developer.android.com/docs)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android/start)
- [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html)

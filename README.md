# Xomust - Android XO Game

A feature-rich Tic-Tac-Toe (XO) game for Android with offline and online multiplayer modes.

## Features

### Offline Modes
- **Player vs Player (PvP)**: Play with a friend on the same device
- **Player vs AI**: Play against AI with three difficulty levels:
  - **Easy**: Random moves
  - **Medium**: Blocks opponent and takes winning moves
  - **Hard**: Unbeatable AI using minimax algorithm

### Online Multiplayer
- Real-time gameplay using Firebase Realtime Database
- Create or join game rooms by name
- Automatic turn synchronization
- Disconnect handling

### Authentication
- Google Sign-In integration (Gmail only)
- Custom username selection
- Profile image upload to Firebase Storage
- User session management

### Additional Features
- Clean, modern Material Design UI
- About section with developer info
- Portrait orientation lock
- Responsive game board

## Tech Stack

- **Language**: Java
- **UI Framework**: AndroidX
- **Backend**: Firebase (Realtime Database, Storage, Authentication)
- **Authentication**: Google Sign-In
- **Minimum SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: 34 (Android 14)

## Setup Instructions

### Prerequisites
1. Android Studio Arctic Fox or later
2. JDK 8 or later
3. Android SDK

### Firebase Setup
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use an existing one
3. Add an Android app to your Firebase project
   - Package name: `com.xomust.game`
4. Download `google-services.json` and replace the placeholder file in `app/`
5. Enable the following Firebase services:
   - **Authentication**: Enable Google Sign-In provider
   - **Realtime Database**: Create a database in test mode
   - **Storage**: Create a storage bucket

### Google Sign-In Configuration
1. In Firebase Console, go to Authentication > Sign-in method
2. Enable Google Sign-In
3. Add your SHA-1 fingerprint:
   ```bash
   keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore
   ```
4. Download the updated `google-services.json`
5. Copy the Web Client ID from Firebase Console
6. Replace `YOUR_WEB_CLIENT_ID_HERE` in `app/src/main/res/values/strings.xml` with your Web Client ID

### Building the Project
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Configure Firebase as described above
5. Build and run the project

```bash
# Build from command line
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## Project Structure

```
app/src/main/java/com/xomust/game/
├── activities/          # All activity classes
│   ├── AboutActivity.java
│   ├── GameActivity.java
│   ├── LoginActivity.java
│   ├── MainActivity.java
│   └── OnlineGameActivity.java
├── ai/                  # AI player implementation
│   └── AIPlayer.java
├── models/              # Data models
│   ├── GameBoard.java
│   ├── GameRoom.java
│   └── User.java
└── utils/               # Utility classes
    └── FirebaseManager.java
```

## Usage

1. **Sign In**: Use your Google account to sign in
2. **Choose Username**: Select a unique username and optional profile image
3. **Main Menu**: Choose between offline or online modes
4. **Offline Mode**: 
   - Select PvP or PvAI
   - If PvAI, choose difficulty level
5. **Online Mode**:
   - Create a new room with a unique name
   - Or join an existing room by entering its name
6. **Play**: Take turns placing X's and O's until someone wins or it's a draw

## About

- **Developer**: مصطفى عايد
- **Version**: 1.0

## License

This project is available for educational and personal use.

## Notes

- The app requires an active internet connection for online features
- Make sure to configure Firebase properly before running
- For production use, update Firebase security rules appropriately

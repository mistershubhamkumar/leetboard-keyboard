# LeetBoard Keyboard

This project is a native Android Input Method Editor (IME) keyboard built in Kotlin.

## Features
- ENGLISH QWERTY mode
- HINDI Devanagari keyboard mode
- LEETSPEAK mode with dictionary-based conversion
- Material-styled modern keyboard UI
- SharedPreferences-based custom rules editor
- IME configuration and system keyboard enablement guidance

## Setup
1. Open the project in Android Studio.
2. Let Gradle sync complete.
3. Build and run on a device or emulator.
4. Go to Settings → Languages & Input → On-screen keyboard → Manage keyboards.
5. Enable LeetBoard Keyboard.
6. Choose LeetBoard Keyboard as the current keyboard.
7. Use the mode key on the keyboard to switch between EN, हिंदी, and 1337.

## Notes
- Custom leet rules can be edited from the included SettingsActivity.
- The app uses SharedPreferences and JSON to persist user-defined char/word rules.
- The IME is declared in `AndroidManifest.xml` and `res/xml/method.xml`.

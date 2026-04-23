# NotifyBridge v0.1.0

First public release candidate of NotifyBridge.

## Highlights

- Android-native notification forwarding with user-approved notification access
- Local filtering by app, keywords, ongoing state, empty body, and dedupe window
- Reliable delivery pipeline backed by Room, Outbox state, and WorkManager retries
- Multiple delivery channels:
  - Bark
  - Telegram
  - Slack
  - Email
- Dashboard, logs, delivery detail view, and settings UI built with Jetpack Compose
- System-language aware UI strings
- Public privacy policy and Play-ready disclosure flow

## Release assets

- `app-release.aab`
  - Play Console upload package
- `app-universal-release.apk`
  - Single APK for most manual installs
- `app-arm64-v8a-release.apk`
  - Optimized for most modern phones
- `app-armeabi-v7a-release.apk`
  - For older 32-bit ARM devices
- `app-x86_64-release.apk`
  - For x86_64 emulators and a small number of compatible devices

## Notes

- Min Android version: 8.0 (API 26)
- Target Android version: API 35
- Package name: `uk.deprecated.notifybridge`
- Website: https://ngcat.uk/
- Privacy policy: https://ngcat.uk/privacy-policy.html

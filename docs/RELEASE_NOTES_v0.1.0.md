# NotifyBridge v0.1.0

First public prerelease build of NotifyBridge.

## What it does

NotifyBridge is an Android app that listens to notifications after user consent, applies local filter rules, and forwards selected notifications to:

- Bark
- Telegram
- Slack
- Email

It also includes:

- Local outbox and retry handling
- Delivery logs and detail view
- App-based and keyword-based filtering
- System-language aware UI
- Public privacy policy and Play-ready disclosure flow

## Which file should you download?

For most people:

- `app-universal-release.apk`
  - Best choice for manual install on a real Android phone

For specific device types:

- `app-arm64-v8a-release.apk`
  - Best for most modern Android phones and tablets
- `app-armeabi-v7a-release.apk`
  - For older 32-bit ARM Android devices
- `app-x86_64-release.apk`
  - For x86_64 Android emulators and a small number of compatible devices

For Google Play Console:

- `app-release.aab`
  - Upload package for Play Internal testing / production tracks

## Install notes

- Minimum Android version: 8.0 (API 26)
- Target Android version: API 35
- Package name: `uk.deprecated.notifybridge`
- If you are installing manually, Android may ask you to allow installs from unknown sources
- If you are not sure which APK to use, choose `app-universal-release.apk`

## SHA-256 checksums

```text
app-universal-release.apk  5675dcb15138f56afa8f0151125e63be0184eea5ff6b6a7270aed50ca07cce07
app-arm64-v8a-release.apk  b5956f74fbf734c282fef6fa593a966ca50d2da257abe6e522a2059ed55655ab
app-armeabi-v7a-release.apk  85bb4f88ac52e0f7507a61c16c2c0fe6cde2108fbe743a38727998364c421abe
app-x86_64-release.apk  91528099ab364bc26357d5c1bb109b3ed31a4b5787d7ef0bbb2be55a764cd57d
app-release.aab  ae268cf33555475f14433e008dc11f112fa936796190f7c80ff968fbdb08ae4c
```

## Links

- Website: https://ngcat.uk/
- Privacy policy: https://ngcat.uk/privacy-policy.html
- Repository: https://github.com/naughtyGitCat/br1dge

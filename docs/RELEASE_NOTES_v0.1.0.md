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
- Package name: `uk.ngcat.notifybridge`
- If you are installing manually, Android may ask you to allow installs from unknown sources
- If you are not sure which APK to use, choose `app-universal-release.apk`

## SHA-256 checksums

```text
app-universal-release.apk  38101efd82482f14f1dd5af1e1d8b290b317a6a9f53e8fc00b19c9c343196d4d
app-arm64-v8a-release.apk  518257420db7a08833f8e2b29fd2c0b02e2f6134e72e5f0a45877034fc5615b9
app-armeabi-v7a-release.apk  2d41ee16e098ef8cb8422b8484121bf6fc142bfc784b0fc7ce5b7d0911f15372
app-x86_64-release.apk  4361f4e051d43ba756db0bc1295605c9e2fcd0029478f8b13d0d5be68fd4d6ce
app-release.aab  b026f23fc6e2c122aafa255bfc0218656356c7807c778fd1394ef662e9240a34
```

## Links

- Website: https://ngcat.uk/
- Privacy policy: https://ngcat.uk/privacy-policy.html
- Repository: https://github.com/naughtyGitCat/br1dge

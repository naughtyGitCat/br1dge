# NotifyBridge Privacy Policy

Publisher: ngcat  
Effective date: 2026-04-20  
Contact: psyduck007@outlook.com  
Website: https://blog.ngcat.uk/

## 1. What NotifyBridge does

NotifyBridge is an Android application that listens to notifications only after the user explicitly grants Android Notification Access permission. The app can filter notifications and forward selected notification content to a destination chosen by the user, such as Bark, Telegram, Slack, or Email.

## 2. Data processed by the app

Depending on user settings, NotifyBridge may process the following categories of data on the device:

- Notification source app name and package name
- Notification title, body text, and subtext
- Notification timestamps and delivery metadata
- Device model and Android version
- Installed app names and package names for app-based forwarding rules
- User-provided delivery configuration such as webhook URLs, bot tokens, or SMTP settings

## 3. Why the data is processed

- To decide which notifications should be forwarded
- To show local delivery logs, retry state, and diagnostic information on the device
- To send selected notifications to the delivery channel configured by the user

## 4. Data sharing

When forwarding is enabled, NotifyBridge transmits selected notification data to the third-party service chosen by the user. Depending on configuration, this may include:

- Bark servers
- Telegram Bot API
- Slack Incoming Webhooks
- SMTP email providers

Those third parties process data under their own privacy policies and terms.

## 5. Local storage

NotifyBridge stores settings, delivery logs, and retry queue records locally on the user’s device. Credentials used for automation, such as channel tokens, webhook URLs, or SMTP settings, may also be stored locally on the device in encrypted local storage to enable forwarding.

## 6. Data retention

Local logs and queued delivery records remain on the device until they are cleared by the user, overwritten by later cleanup behavior in the app, or removed when the app is uninstalled. Forwarded data retained by Bark, Telegram, Slack, email providers, or any self-hosted endpoint is controlled by those services and not by ngcat.

## 7. Security

NotifyBridge uses Android platform APIs and network libraries to process and transmit data. When a configured endpoint supports HTTPS or TLS, the app can use encrypted transport. Some user-configured self-hosted endpoints may use HTTP or other settings chosen by the user. Users are responsible for the security of third-party services and endpoints they configure.

Sensitive channel configuration is stored locally on-device in encrypted local storage for automation purposes. Users should still secure their device and avoid entering credentials for services they do not trust on that device.

## 8. User choices and control

- Enable or disable forwarding at any time
- Revoke Notification Access in Android settings
- Remove configured channel credentials
- Clear local logs and exported debug files
- Uninstall the app to remove locally stored app data, subject to Android backup behavior

## 9. Children

NotifyBridge is not designed for children. The app should only be used by people who understand the consequences of forwarding notification content to external services.

## 10. Changes to this policy

This policy may be updated as NotifyBridge changes. The latest version should be published at https://blog.ngcat.uk/privacy-policy.html or the latest public URL used for the Google Play listing.

## 11. Contact

If you have privacy questions about NotifyBridge, contact ngcat at psyduck007@outlook.com.

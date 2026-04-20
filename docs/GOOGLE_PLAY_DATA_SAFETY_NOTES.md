# Google Play Data Safety Notes

This file is a working draft for Play Console's Data safety section. Review before submission.

## Data categories likely relevant to NotifyBridge

### Personal info

- Email address
  - Relevant when the user configures SMTP sender or recipient addresses

### App activity

- In-app interactions
  - Delivery logs and retry actions are shown locally in the app

### Messages

- Other in-app messages or notification content
  - The app processes notification title, text, and subtext

### App info and performance

- Diagnostics
  - Delivery logs, error summaries, and retry metadata may be retained locally

### Device or other IDs

- Installed app inventory / package names
  - Used for app-based forwarding and block rules

## Collection and sharing considerations

- Notification content is processed on-device and may be transmitted to user-configured third-party endpoints
- Installed app names and package names are used locally for filtering
- Channel credentials are stored locally in encrypted storage
- Data sharing depends on the user-selected delivery channel:
  - Bark
  - Telegram
  - Slack
  - Email / SMTP provider

## Suggested console review checklist

- Verify whether installed app inventory should be declared under device or other IDs / app activity depending on the latest Play Console taxonomy
- Verify whether notification content should be declared as messages, app activity, or both
- Mark transmission to Bark/Telegram/Slack/Email as data shared with third parties when forwarding is enabled
- Make sure the answers match:
  - in-app prominent disclosure
  - public privacy policy
  - actual runtime behavior

# Google Play Release Checklist

## Policy and disclosure

- Publish a public privacy policy over HTTPS.
- Add the privacy policy URL to Play Console.
- Keep the in-app prominent disclosure screen enabled before first use.
- Verify the disclosure text matches actual behavior for notifications, installed-app filtering, and forwarding channels.

## Data safety

- Prepare Play Data safety answers for notification content, installed app inventory, device information, and third-party forwarding.
- Declare all destinations that may receive forwarded data, including Bark, Telegram, Slack, and Email providers.

## Security

- Prefer HTTPS-only endpoints for release builds.
- Review whether release builds should continue to allow cleartext traffic.
- Move tokens and SMTP credentials to encrypted storage before public launch.

## Release artifacts

- Configure upload signing key.
- Enable Play App Signing.
- Build and validate a release AAB.
- Upload the AAB to Internal testing before wider rollout.

## Store listing

- App icon
- Feature graphic
- Screenshots
- Short description
- Full description
- Support contact details
- App category and content rating

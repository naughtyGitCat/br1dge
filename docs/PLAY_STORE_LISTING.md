# Google Play Store Listing Draft

This file is a ready-to-use draft for the main store listing of NotifyBridge.

## App details

- App name: `NotifyBridge`
- Default language: `en-US`
- Category suggestion: `Tools`
- Contact email: `psyduck007@outlook.com`
- Website: `https://blog.ngcat.uk/`
- Privacy policy URL: `https://naughtygitcat.github.io/br1dge/privacy-policy.html`

## Short description

Forward Android notifications to Bark, Telegram, Slack, or Email with filters.

Length target: within 80 characters.

## Full description

NotifyBridge is a native Android notification forwarding app built for automation, alerting, and personal workflows.

After you grant Android Notification Access, NotifyBridge can listen to real-time notifications, apply filters, store delivery logs locally, and forward selected notifications to the channel you choose.

Supported delivery channels:

- Bark
- Telegram
- Slack
- Email

Key features:

- Forward notifications from selected apps only
- Block notifications from apps you do not want to forward
- Filter by keywords
- Deduplicate repeated notifications within a configurable time window
- Retry failed deliveries with persistent queue and WorkManager
- View delivery logs, retry state, and error details on-device
- Test delivery channels from the settings page
- Optional loop prevention for Telegram and Slack
- Optional clearing of the original local notification after successful forwarding
- Localized UI with system-language support

NotifyBridge is designed for users who want a lightweight bridge between Android notifications and their own messaging or automation systems. Typical use cases include:

- Sending important alerts from specific apps to Telegram or Slack
- Forwarding OTP, monitoring, or service notifications to a personal alert channel
- Routing Android notifications into self-hosted Bark services
- Building simple device-based notification automations without running a foreground service

Privacy and control:

- Forwarding is disabled by default until you enable it
- The app shows a prominent disclosure before first use
- You can revoke Notification Access at any time in Android settings
- Delivery logs and settings are stored locally on your device
- Sensitive channel configuration is stored in encrypted local storage

Important notes:

- NotifyBridge only processes real-time notifications after permission is granted
- Notification content availability depends on Android and the source app
- Third-party delivery services such as Bark, Telegram, Slack, or email providers process forwarded data under their own policies

If you use Android notifications as part of your personal workflow, operations setup, or lightweight automation pipeline, NotifyBridge gives you a focused and maintainable bridge without unnecessary complexity.

Length target: within 4000 characters.

## Chinese listing draft

### Short description

把 Android 通知按规则转发到 Bark、Telegram、Slack 或 Email。

### Full description

NotifyBridge 是一款原生 Android 通知转发工具，适合自动化、告警同步和个人工作流场景。

在你授予 Android 通知使用权后，NotifyBridge 可以监听实时通知，按规则过滤，把命中的通知写入本地队列，并转发到你配置的目标渠道。

支持的转发渠道：

- Bark
- Telegram
- Slack
- Email

核心能力：

- 按应用选择哪些通知允许转发
- 按应用选择哪些通知禁止转发
- 支持关键词白名单和黑名单
- 支持去重，避免短时间内重复发送同类通知
- 失败后自动重试，并保留本地队列与日志
- 可查看投递日志、错误详情和重试状态
- 可直接测试转发渠道是否连通
- Telegram / Slack 支持可选防回环
- 可选“转发成功后清除本机原通知”
- 界面默认跟随系统语言

典型使用场景：

- 把指定应用的重要通知同步到 Telegram 或 Slack
- 把验证码、监控、服务通知转发到个人通知渠道
- 把 Android 设备作为 Bark 自建通知桥接端
- 不依赖常驻前台服务，做轻量通知自动化

隐私与控制：

- 默认不启用转发，需用户主动开启
- 首次使用前会显示显著披露说明
- 你可以随时在系统设置中撤销通知访问权限
- 日志和设置保存在本地设备
- 敏感渠道配置使用本地加密存储

注意：

- 仅处理授权后的实时通知，不抓取历史通知
- 某些通知内容是否可读，取决于 Android 系统和来源应用
- Bark、Telegram、Slack、邮件服务等第三方渠道对转发数据的处理受其各自政策约束

## Screenshot plan

Suggested first 5 screenshots:

1. Dashboard
   - Highlight listener status, pending queue, retry state, and last success
2. Settings basic section
   - Highlight channel selection and forwarding toggle
3. App forwarding rules
   - Highlight per-app allow/block controls
4. Delivery logs
   - Highlight search, status filters, and retry state
5. Delivery detail
   - Highlight request/response diagnostics and retry advice

## Feature graphic idea

Suggested headline:

- Android notifications, routed your way

Suggested subheadline:

- Filter, forward, retry

## Internal testing checklist

1. Create the app in Play Console
2. Add app name, default language, app category, contact email, website, privacy policy URL
3. Upload the signed release AAB
4. Configure Play App Signing
5. Fill Data safety using `docs/GOOGLE_PLAY_DATA_SAFETY_NOTES.md`
6. Add at least a basic store listing and screenshots
7. Publish to Internal testing first

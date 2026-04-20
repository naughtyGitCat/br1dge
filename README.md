# NotifyBridge

NotifyBridge 是一个 Android 原生通知转发桥接应用。它通过 `NotificationListenerService` 监听设备收到的系统通知，将符合规则的通知入库、写入 Outbox，再通过 `WorkManager` 异步转发到 Bark、Telegram、Slack 或 Email。

## 总体方案

业务主链路：

1. `BridgeNotificationListenerService` 收到实时通知
2. `NotificationParser` 提取可读字段，容忍 `title/text/subText` 为空
3. `HandleIncomingNotificationUseCase` 执行 domain 层过滤与去重判定
4. 通过 `DeliveryLogRepository` 将通知写入 `notification_events` 和 `outbox`
5. `DeliveryWorkScheduler` 调度 `DeliveryWorker`
6. `SendPendingNotificationsUseCase` 从 Outbox 拉取待发送事件，调用 `ForwardRepository`
7. `Retrofit + OkHttp + Kotlinx Serialization` 发送到配置的 Webhook
8. 成功更新为 `SUCCESS`，失败根据错误类型更新为 `FAILED` 或 `RETRYING`

关键设计取舍：

- 不默认使用 Foreground Service，避免常驻通知和额外系统限制
- `NotificationListenerService` 只负责轻量解析和入队，不直接做长时间网络阻塞
- 可配置项全部通过 `DataStore` 持久化，不使用 `SharedPreferences`
- 结构化记录通过 Room 管理，便于日志、详情、重试与后续扩展
- 重试通过 `WorkManager` 承担，支持网络约束、指数退避和重启后恢复

## 项目结构

```text
app/src/main/java/com/example/notifybridge
├── app
├── core
│   ├── common
│   ├── datastore
│   ├── database
│   ├── network
│   └── ui
├── data
│   ├── mapper
│   └── repository
├── domain
│   ├── model
│   ├── repository
│   └── usecase
├── feature
│   ├── dashboard
│   ├── detail
│   ├── logs
│   └── settings
└── system
    ├── receiver
    ├── service
    ├── util
    └── worker
```

## 运行方式

1. 用 Android Studio 打开项目根目录。
2. 等待 Gradle Sync 完成。
3. 使用 Android 8.0+ 真机运行，建议 Android 13/14。
4. 首次运行后进入应用首页与设置页完成授权和转发渠道配置。
5. 首次进入应用时，会先看到显著披露与隐私政策入口；同意后才会请求通知权限并进入主界面。

命令行构建：

- `./gradlew :app:assembleDebug`

当前配置：

- `minSdk = 26`
- `targetSdk = 35`
- 包名：`uk.deprecated.notifybridge`
- 语言：默认跟随系统语言，当前提供 `中文(简体)` 与 `English`

## 上架准备

- Google Play 上架整改清单：`docs/GOOGLE_PLAY_RELEASE_CHECKLIST.md`
- 隐私政策正式文案：`docs/PRIVACY_POLICY.md`
- 隐私政策网页文件：`docs/privacy-policy.html`
- 隐私政策模板：`docs/PRIVACY_POLICY_TEMPLATE.md`

当前工程已经包含：

- 首启显著披露与同意
- 应用内隐私政策入口
- 多语言文案

正式上架前仍建议补齐：

- 公开 HTTPS 隐私政策 URL
- 加密存储渠道密钥与 SMTP 凭据
- Release AAB 签名与 Play App Signing
- 面向公开版的网络安全策略审查

## 如何授予通知访问权限

应用内路径：

- Dashboard 页面点击“通知访问设置”

系统常见路径：

- Android 原生：`设置 -> 通知 -> 设备与应用通知 -> 通知使用权 / Notification access`
- 某些 ROM 可能显示为：`设置 -> 应用 -> 特殊应用权限 -> 通知使用权`

开启后请确认 `NotifyBridge Notification Access` 已被允许。

## 电池优化设置

应用内可点击“电池优化设置”跳转。不同 ROM 的路径差异较大，常见位置包括：

- `设置 -> 电池 -> 电池优化`
- `设置 -> 应用 -> 特殊应用权限 -> 忽略电池优化`
- 厂商安全中心/后台管理页面

建议将 NotifyBridge 加入：

- 自启动白名单
- 后台运行白名单
- 电池优化忽略名单

## 转发渠道配置

当前版本一次只启用一个转发渠道。可选：

- Bark
- Telegram
- Slack
- Email

在 `Settings -> 基础设置 -> 转发渠道` 中切换。

### Bark 配置

Settings 页面支持配置：

- 启用转发总开关
- 转发成功后清除本机原通知
- Bark Server URL
- Bark Device Key
- Bark Level / Action / Sound 下拉选择
- Bark Group 支持模板下拉：
  `{本应用名称}` / `{本设备名称}` / `{本应用名称}@{本设备名称}` / `{自定义}`
- 应用白名单/黑名单
- 关键词白名单/黑名单
- 排除系统通知
- 排除 ongoing 通知
- 排除空正文
- 去重秒数
- 自动重试

当前实现按 Bark 官方教程的 JSON 方式发送：

- `POST https://api.day.app/push`
- `Content-Type: application/json`
- 在请求体里传 `device_key`

服务端兼容项：

- 默认 Bark Server URL：`https://api.day.app`
- 也支持自建 Bark 服务地址
- 为兼容局域网或自签环境下的自建 Bark，当前 Android 客户端允许明文 `http://` 地址；如无明确需要，仍建议优先使用 `https://`

### Telegram 配置

需要准备：

- Telegram Bot Token
- Chat ID

推荐步骤：

1. 在 Telegram 中找到 `@BotFather`
2. 发送 `/newbot` 创建机器人，拿到 `Bot Token`
3. 把机器人拉进你的目标聊天，或者直接给机器人发一条消息
4. 通过 Telegram Bot API 获取 `Chat ID`
5. 在 NotifyBridge 里填写：
   - `Telegram Bot Token`
   - `Telegram Chat ID`
   - 如有论坛/话题，再填写 `Telegram Thread ID`

说明：

- 可选开启“静默发送”
- 可选开启 Telegram Markdown
- 可选开启“防止渠道通知回环”；开启后，当当前转发渠道为 Telegram 时，会自动忽略 Telegram 客户端自身通知

### Slack 配置

需要准备：

- Slack Incoming Webhook URL

推荐步骤：

1. 在 Slack App 配置里启用 `Incoming Webhooks`
2. 为目标频道生成一个 Webhook URL
3. 在 NotifyBridge 里填写：
   - `Slack Webhook URL`
   - 可选 `Slack display name`
   - 可选 `Slack icon emoji`

说明：

- 可选开启“防止渠道通知回环”；开启后，当当前转发渠道为 Slack 时，会自动忽略 Slack 客户端自身通知

### Email 配置

需要准备：

- SMTP Host / Port
- SMTP 用户名与密码
- 发件地址与收件地址

常见建议：

- 587 + STARTTLS：最常见
- 465 + SSL/TLS：一些服务商使用
- 很多邮箱服务商要求“应用专用密码”，不是网页登录密码

在 NotifyBridge 里填写：

- `SMTP host`
- `SMTP port`
- `Security mode`
- `SMTP username`
- `SMTP password`
- `From address`
- `To address`
- 可选 `Subject prefix`

## 如何测试

1. 在 Settings 里选择一个转发渠道并完成该渠道配置。
2. 打开“启用转发总开关”和“启用过滤规则”。
3. 如需最简单验证，可先不配置白名单。
4. Dashboard 点击“生成测试通知”。
5. 观察 Logs 页面是否出现新记录。
6. 也可以在 Settings 点击“测试发送”，验证当前渠道链路本身。

如果开启“转发成功后清除本机原通知”：

- 仅会在转发成功后尝试清除原通知
- 当前只会清除仍然可清除的通知，系统限制或不可清除通知会跳过
- 清除动作依赖通知监听服务仍处于连接状态

## GitHub Actions

仓库内置了 Android 产物打包工作流：

- 文件位置：`.github/workflows/build-apk.yml`
- 触发方式：`push main`、`pull_request`、手动 `workflow_dispatch`
- 当前产物：
  - `Debug APK`
  - `Release AAB`
- 产物下载：
  - `notifybridge-debug-apk`
  - `notifybridge-release-aab`
- CI 构建入口：`./gradlew :app:assembleDebug :app:bundleRelease`

## 状态定义

Outbox 状态：

- `PENDING`：已入队，等待发送
- `RETRYING`：发送中或将继续重试
- `SUCCESS`：发送成功
- `FAILED`：永久失败或自动重试关闭

错误模型通过 `ForwardError` sealed class 表达，区分：

- 未配置 endpoint
- 网络不可用
- 连接失败
- 超时
- HTTP 错误
- 序列化错误
- 未知错误

## 数据存储

- DataStore：保存应用设置和过滤规则
- Room：
  - `notification_events`
  - `outbox`
  - `delivery_attempts`

Room migration 预留点：

- `NotifyBridgeDatabase.Companion.migrations`

当前数据库版本为 `1`，后续升级时可在该位置追加 Migration。

## 已知限制

- Android 对通知内容的可见性受系统和来源 App 限制，`title/text` 可能为空
- 厂商 ROM 可能限制后台调度，导致延迟增大或恢复不及时
- `NotificationListenerService` 只能处理授权后的实时通知，不会抓取历史通知
- 当前 MVP 只支持一个 Webhook endpoint
- 日志列表目前是懒加载列表，但尚未做真正分页数据库查询
- 当前导出调试信息为本地文本快照，未接入系统分享或 SAF
- 本地命令行构建需要可用的 Java 17 环境

## 常见 ROM 限制

请特别注意以下 ROM 可能需要额外设置：

- MIUI / HyperOS：自启动、后台弹出界面、无限制省电
- ColorOS / realme UI：后台冻结、自动启动管理
- EMUI / HarmonyOS：应用启动管理、后台活动
- One UI：电池使用限制、深度休眠应用

如果发现通知已监听但转发恢复慢，请优先检查：

1. 通知访问权限是否仍开启
2. 电池优化是否放开
3. 自启动/后台白名单是否配置
4. Bark 服务地址和 Device Key 是否正确

## 后续扩展建议

- 多 endpoint 与路由规则
- HMAC / 签名校验
- 模板化 payload
- 配置导入导出
- 云端同步
- 更细粒度分页和搜索
- 更完整的尝试历史详情页

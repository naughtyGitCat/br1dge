# NotifyBridge

NotifyBridge 是一个 Android 原生通知转发桥接应用。它通过 `NotificationListenerService` 监听设备收到的系统通知，将符合规则的通知入库、写入 Outbox，再通过 `WorkManager` 异步转发到远端 Webhook。

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
4. 首次运行后进入应用首页与设置页完成授权和 Bark 配置。

命令行构建：

- `./gradlew :app:assembleDebug`

当前配置：

- `minSdk = 26`
- `targetSdk = 35`
- 包名：`uk.deprecated.notifybridge`

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

## Bark 配置

Settings 页面支持配置：

- 启用转发总开关
- Bark Server URL
- Bark Device Key
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

## 如何测试

1. 在 Settings 填入一个可用的 Bark Server URL 和 Bark Device Key。
2. 打开“启用转发总开关”和“启用过滤规则”。
3. 如需最简单验证，可先不配置白名单。
4. Dashboard 点击“生成测试通知”。
5. 观察 Logs 页面是否出现新记录。
6. 也可以在 Settings 点击“测试发送”，验证 Bark 推送链路本身。

## GitHub Actions

仓库内置了 APK 打包工作流：

- 文件位置：`.github/workflows/build-apk.yml`
- 触发方式：`push main`、`pull_request`、手动 `workflow_dispatch`
- 当前产物：`Debug APK`
- 产物下载：GitHub Actions 页面中的 `notifybridge-debug-apk`
- CI 构建入口：`./gradlew :app:assembleDebug`

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

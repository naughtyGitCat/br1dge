# Google Play 内测自动发布

本项目通过 GitHub Actions 自动构建 signed AAB，并上传到 Google Play Internal testing。

## 触发方式

- 手动触发：GitHub 仓库 -> Actions -> `Publish Play Internal Test` -> `Run workflow`
- 打 tag 触发：推送 `v*` tag，例如 `v0.1.1`

工作流文件：

- `.github/workflows/publish-play-internal.yml`

## 需要配置的 GitHub Secrets

已有签名相关 secrets：

- `NB_UPLOAD_STORE_B64`
- `NB_UPLOAD_STORE_PASSWORD`
- `NB_UPLOAD_KEY_ALIAS`
- `NB_UPLOAD_KEY_PASSWORD`

新增 Play API secret：

- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`

`GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` 的值是 Google Cloud service account JSON key 的完整文本内容。

## Google Play Console 配置步骤

1. 在 Play Console 打开目标应用。
2. 进入 `Setup -> API access`。
3. 关联一个 Google Cloud project。
4. 创建或选择 service account，并生成 JSON key。
5. 回到 Play Console，把 service account 加为用户。
6. 给该 service account 授权目标应用的发布权限，至少需要能管理 internal testing release。
7. 把 JSON key 完整内容保存到 GitHub secret：`GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`。

Google 官方文档：

- Google Play Developer API getting started: https://developers.google.com/android-publisher/getting_started
- Google Cloud service account: https://cloud.google.com/iam/docs/creating-managing-service-accounts

## 注意事项

- Play Console 里必须已经创建过应用，并且包名是 `uk.ngcat.notifybridge`。
- 每次上传到 Play 的 `versionCode` 必须递增。
- 第一次启用 API 或新增 service account 权限后，Google Play 可能需要几分钟到数小时生效。
- 如果开启 Managed publishing，API 可以上传内测 release，但部分“最终发布/上线”动作可能仍需要在 Play Console 里确认。

package com.example.notifybridge.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifybridge.domain.model.AppSettings

@Composable
fun SettingsScreenRoute(
    contentPadding: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun SettingsScreen(
    contentPadding: PaddingValues,
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    var barkServerUrl by remember(uiState.settings.barkServerUrl) { mutableStateOf(uiState.settings.barkServerUrl) }
    var barkDeviceKey by remember(uiState.settings.barkDeviceKey) { mutableStateOf(uiState.settings.barkDeviceKey) }
    var barkDeviceKeys by remember(uiState.settings.barkDeviceKeys) { mutableStateOf(uiState.settings.barkDeviceKeys.joinToString(",")) }
    var barkLevel by remember(uiState.settings.barkLevel) { mutableStateOf(uiState.settings.barkLevel) }
    var barkVolume by remember(uiState.settings.barkVolume) { mutableStateOf(uiState.settings.barkVolume?.toString().orEmpty()) }
    var barkBadge by remember(uiState.settings.barkBadge) { mutableStateOf(uiState.settings.barkBadge?.toString().orEmpty()) }
    var barkCall by remember(uiState.settings.barkCall) { mutableStateOf(uiState.settings.barkCall) }
    var barkAutoCopy by remember(uiState.settings.barkAutoCopy) { mutableStateOf(uiState.settings.barkAutoCopy) }
    var barkCopy by remember(uiState.settings.barkCopy) { mutableStateOf(uiState.settings.barkCopy) }
    var barkSound by remember(uiState.settings.barkSound) { mutableStateOf(uiState.settings.barkSound) }
    var barkIcon by remember(uiState.settings.barkIcon) { mutableStateOf(uiState.settings.barkIcon) }
    var barkImage by remember(uiState.settings.barkImage) { mutableStateOf(uiState.settings.barkImage) }
    var barkGroup by remember(uiState.settings.barkGroup) { mutableStateOf(uiState.settings.barkGroup) }
    var barkCiphertext by remember(uiState.settings.barkCiphertext) { mutableStateOf(uiState.settings.barkCiphertext) }
    var barkIsArchive by remember(uiState.settings.barkIsArchive) { mutableStateOf(uiState.settings.barkIsArchive ?: true) }
    var barkUrl by remember(uiState.settings.barkUrl) { mutableStateOf(uiState.settings.barkUrl) }
    var barkAction by remember(uiState.settings.barkAction) { mutableStateOf(uiState.settings.barkAction) }
    var barkNotificationId by remember(uiState.settings.barkNotificationId) { mutableStateOf(uiState.settings.barkNotificationId) }
    var barkDelete by remember(uiState.settings.barkDelete) { mutableStateOf(uiState.settings.barkDelete) }
    var barkUseMarkdown by remember(uiState.settings.barkUseMarkdown) { mutableStateOf(uiState.settings.barkUseMarkdown) }
    var allowedPackages by remember(uiState.settings.filterRuleSet.allowedPackages) {
        mutableStateOf(uiState.settings.filterRuleSet.allowedPackages.joinToString(","))
    }
    var blockedPackages by remember(uiState.settings.filterRuleSet.blockedPackages) {
        mutableStateOf(uiState.settings.filterRuleSet.blockedPackages.joinToString(","))
    }
    var whitelist by remember(uiState.settings.filterRuleSet.keywordWhitelist) {
        mutableStateOf(uiState.settings.filterRuleSet.keywordWhitelist.joinToString(","))
    }
    var blacklist by remember(uiState.settings.filterRuleSet.keywordBlacklist) {
        mutableStateOf(uiState.settings.filterRuleSet.keywordBlacklist.joinToString(","))
    }
    var dedupeSeconds by remember(uiState.settings.filterRuleSet.dedupeWindowSeconds) {
        mutableStateOf(uiState.settings.filterRuleSet.dedupeWindowSeconds.toString())
    }
    val draft = SettingsDraft(
        forwardingEnabled = uiState.settings.forwardingEnabled,
        cancelNotificationOnSuccess = uiState.settings.cancelNotificationOnSuccess,
        barkServerUrl = barkServerUrl,
        barkDeviceKey = barkDeviceKey,
        barkDeviceKeys = barkDeviceKeys,
        barkLevel = barkLevel,
        barkVolume = barkVolume,
        barkBadge = barkBadge,
        barkCall = barkCall,
        barkAutoCopy = barkAutoCopy,
        barkCopy = barkCopy,
        barkSound = barkSound,
        barkIcon = barkIcon,
        barkImage = barkImage,
        barkGroup = barkGroup,
        barkCiphertext = barkCiphertext,
        barkIsArchive = barkIsArchive,
        barkUrl = barkUrl,
        barkAction = barkAction,
        barkNotificationId = barkNotificationId,
        barkDelete = barkDelete,
        barkUseMarkdown = barkUseMarkdown,
        allowedPackages = allowedPackages,
        blockedPackages = blockedPackages,
        keywordWhitelist = whitelist,
        keywordBlacklist = blacklist,
        dedupeSeconds = dedupeSeconds,
        filtersEnabled = uiState.settings.filterRuleSet.enabled,
        excludeSystem = uiState.settings.filterRuleSet.excludeSystemNotifications,
        excludeOngoing = uiState.settings.filterRuleSet.excludeOngoingNotifications,
        excludeEmpty = uiState.settings.filterRuleSet.excludeEmptyTextNotifications,
        autoRetry = uiState.settings.filterRuleSet.autoRetryEnabled,
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("基础设置")
                    SwitchRow(
                        title = "启用转发总开关",
                        checked = uiState.settings.forwardingEnabled,
                        onCheckedChange = {
                            save(
                                current = uiState.settings,
                                onAction = onAction,
                                barkServerUrl = barkServerUrl,
                                barkDeviceKey = barkDeviceKey,
                                allowedPackages = allowedPackages,
                                blockedPackages = blockedPackages,
                                whitelist = whitelist,
                                blacklist = blacklist,
                                dedupeSeconds = dedupeSeconds,
                                forwardingEnabled = it,
                            )
                        }
                    )
                    SwitchRow(
                        title = "启用过滤规则",
                        checked = uiState.settings.filterRuleSet.enabled,
                        onCheckedChange = {
                            save(
                                current = uiState.settings,
                                onAction = onAction,
                                barkServerUrl = barkServerUrl,
                                barkDeviceKey = barkDeviceKey,
                                allowedPackages = allowedPackages,
                                blockedPackages = blockedPackages,
                                whitelist = whitelist,
                                blacklist = blacklist,
                                dedupeSeconds = dedupeSeconds,
                                filtersEnabled = it,
                            )
                        }
                    )
                    SwitchRow(
                        title = "转发成功后清除本机原通知",
                        checked = uiState.settings.cancelNotificationOnSuccess,
                        onCheckedChange = {
                            save(
                                current = uiState.settings,
                                onAction = onAction,
                                barkServerUrl = barkServerUrl,
                                barkDeviceKey = barkDeviceKey,
                                allowedPackages = allowedPackages,
                                blockedPackages = blockedPackages,
                                whitelist = whitelist,
                                blacklist = blacklist,
                                dedupeSeconds = dedupeSeconds,
                                cancelNotificationOnSuccess = it,
                            )
                        }
                    )
                    OutlinedTextField(value = barkServerUrl, onValueChange = { barkServerUrl = it }, label = { Text("Bark Server URL") }, modifier = Modifier.fillMaxWidth())
                    uiState.validation.barkServerUrlError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    OutlinedTextField(value = barkDeviceKey, onValueChange = { barkDeviceKey = it }, label = { Text("Bark Device Key") }, modifier = Modifier.fillMaxWidth())
                    uiState.validation.barkDeviceKeyError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Bark 高级参数")
                            OutlinedTextField(value = barkDeviceKeys, onValueChange = { barkDeviceKeys = it }, label = { Text("Device Keys（逗号分隔，可选）") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = barkLevel, onValueChange = { barkLevel = it }, label = { Text("Level") }, modifier = Modifier.fillMaxWidth())
                            uiState.validation.barkLevelError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            OutlinedTextField(value = barkVolume, onValueChange = { barkVolume = it }, label = { Text("Volume（0-10，可选）") }, modifier = Modifier.fillMaxWidth())
                            uiState.validation.barkVolumeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            OutlinedTextField(value = barkBadge, onValueChange = { barkBadge = it }, label = { Text("Badge（整数，可选）") }, modifier = Modifier.fillMaxWidth())
                            uiState.validation.barkBadgeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            OutlinedTextField(value = barkCopy, onValueChange = { barkCopy = it }, label = { Text("Copy（可选）") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = barkSound, onValueChange = { barkSound = it }, label = { Text("Sound（可选）") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = barkIcon, onValueChange = { barkIcon = it }, label = { Text("Icon URL（可选）") }, modifier = Modifier.fillMaxWidth())
                            uiState.validation.barkIconError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            OutlinedTextField(value = barkImage, onValueChange = { barkImage = it }, label = { Text("Image URL（可选）") }, modifier = Modifier.fillMaxWidth())
                            uiState.validation.barkImageError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            OutlinedTextField(value = barkGroup, onValueChange = { barkGroup = it }, label = { Text("Group（可选）") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = barkCiphertext, onValueChange = { barkCiphertext = it }, label = { Text("Ciphertext（可选）") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = barkUrl, onValueChange = { barkUrl = it }, label = { Text("点击跳转 URL（可选）") }, modifier = Modifier.fillMaxWidth())
                            uiState.validation.barkUrlError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            OutlinedTextField(value = barkAction, onValueChange = { barkAction = it }, label = { Text("Action（可选）") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = barkNotificationId, onValueChange = { barkNotificationId = it }, label = { Text("通知 ID（可选）") }, modifier = Modifier.fillMaxWidth())
                            SwitchRow(title = "Call", checked = barkCall, onCheckedChange = { barkCall = it })
                            SwitchRow(title = "AutoCopy", checked = barkAutoCopy, onCheckedChange = { barkAutoCopy = it })
                            SwitchRow(title = "使用 Markdown 发送", checked = barkUseMarkdown, onCheckedChange = { barkUseMarkdown = it })
                            SwitchRow(title = "保存到 Bark 历史", checked = barkIsArchive, onCheckedChange = { barkIsArchive = it })
                            SwitchRow(title = "Delete（配合 ID 删除）", checked = barkDelete, onCheckedChange = { barkDelete = it })
                        }
                    }
                    OutlinedTextField(value = allowedPackages, onValueChange = { allowedPackages = it }, label = { Text("允许应用包名（逗号分隔）") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = blockedPackages, onValueChange = { blockedPackages = it }, label = { Text("黑名单应用包名（逗号分隔）") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = whitelist, onValueChange = { whitelist = it }, label = { Text("关键词白名单（逗号分隔）") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = blacklist, onValueChange = { blacklist = it }, label = { Text("关键词黑名单（逗号分隔）") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dedupeSeconds, onValueChange = { dedupeSeconds = it }, label = { Text("去重秒数") }, modifier = Modifier.fillMaxWidth())
                    uiState.validation.dedupeSecondsError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    SwitchRow(
                        title = "排除系统通知",
                        checked = uiState.settings.filterRuleSet.excludeSystemNotifications,
                        onCheckedChange = {
                            save(current = uiState.settings, onAction = onAction, barkServerUrl = barkServerUrl, barkDeviceKey = barkDeviceKey, allowedPackages = allowedPackages, blockedPackages = blockedPackages, whitelist = whitelist, blacklist = blacklist, dedupeSeconds = dedupeSeconds, excludeSystem = it)
                        }
                    )
                    SwitchRow(
                        title = "排除 ongoing 通知",
                        checked = uiState.settings.filterRuleSet.excludeOngoingNotifications,
                        onCheckedChange = {
                            save(current = uiState.settings, onAction = onAction, barkServerUrl = barkServerUrl, barkDeviceKey = barkDeviceKey, allowedPackages = allowedPackages, blockedPackages = blockedPackages, whitelist = whitelist, blacklist = blacklist, dedupeSeconds = dedupeSeconds, excludeOngoing = it)
                        }
                    )
                    SwitchRow(
                        title = "排除空正文",
                        checked = uiState.settings.filterRuleSet.excludeEmptyTextNotifications,
                        onCheckedChange = {
                            save(current = uiState.settings, onAction = onAction, barkServerUrl = barkServerUrl, barkDeviceKey = barkDeviceKey, allowedPackages = allowedPackages, blockedPackages = blockedPackages, whitelist = whitelist, blacklist = blacklist, dedupeSeconds = dedupeSeconds, excludeEmpty = it)
                        }
                    )
                    SwitchRow(
                        title = "自动重试",
                        checked = uiState.settings.filterRuleSet.autoRetryEnabled,
                        onCheckedChange = {
                            save(current = uiState.settings, onAction = onAction, barkServerUrl = barkServerUrl, barkDeviceKey = barkDeviceKey, allowedPackages = allowedPackages, blockedPackages = blockedPackages, whitelist = whitelist, blacklist = blacklist, dedupeSeconds = dedupeSeconds, autoRetry = it)
                        }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = {
                            onAction(SettingsAction.Save(draft))
                        }, modifier = Modifier.weight(1f)) {
                            Text("保存设置")
                        }
                        Button(
                            onClick = { onAction(SettingsAction.TestSend(draft)) },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.testSendState !is TestSendState.Running,
                        ) {
                            Text(if (uiState.testSendState is TestSendState.Running) "测试中..." else "测试发送")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { onAction(SettingsAction.ClearLogs) }, modifier = Modifier.weight(1f)) {
                            Text("清空日志")
                        }
                        Button(onClick = { onAction(SettingsAction.ExportDebug) }, modifier = Modifier.weight(1f)) {
                            Text("导出调试信息")
                        }
                    }
                    uiState.message?.let { Text(it) }
                    when (val state = uiState.testSendState) {
                        TestSendState.Idle -> Unit
                        TestSendState.Running -> Text("正在请求当前表单配置对应的 Bark 接口...")
                        is TestSendState.Success -> Text("连通性结果：${state.message}")
                        is TestSendState.Failure -> Text("连通性结果：${state.message}")
                    }
                    uiState.exportingPath?.let { Text("导出文件(JSON)：$it") }
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun save(
    current: AppSettings,
    onAction: (SettingsAction) -> Unit,
    barkServerUrl: String,
    barkDeviceKey: String,
    barkDeviceKeys: String = current.barkDeviceKeys.joinToString(","),
    barkLevel: String = current.barkLevel,
    barkVolume: String = current.barkVolume?.toString().orEmpty(),
    barkBadge: String = current.barkBadge?.toString().orEmpty(),
    barkCall: Boolean = current.barkCall,
    barkAutoCopy: Boolean = current.barkAutoCopy,
    barkCopy: String = current.barkCopy,
    barkSound: String = current.barkSound,
    barkIcon: String = current.barkIcon,
    barkImage: String = current.barkImage,
    barkGroup: String = current.barkGroup,
    barkCiphertext: String = current.barkCiphertext,
    barkIsArchive: Boolean = current.barkIsArchive ?: true,
    barkUrl: String = current.barkUrl,
    barkAction: String = current.barkAction,
    barkNotificationId: String = current.barkNotificationId,
    barkDelete: Boolean = current.barkDelete,
    barkUseMarkdown: Boolean = current.barkUseMarkdown,
    allowedPackages: String,
    blockedPackages: String,
    whitelist: String,
    blacklist: String,
    dedupeSeconds: String,
    forwardingEnabled: Boolean = current.forwardingEnabled,
    cancelNotificationOnSuccess: Boolean = current.cancelNotificationOnSuccess,
    filtersEnabled: Boolean = current.filterRuleSet.enabled,
    excludeSystem: Boolean = current.filterRuleSet.excludeSystemNotifications,
    excludeOngoing: Boolean = current.filterRuleSet.excludeOngoingNotifications,
    excludeEmpty: Boolean = current.filterRuleSet.excludeEmptyTextNotifications,
    autoRetry: Boolean = current.filterRuleSet.autoRetryEnabled,
) {
    onAction(SettingsAction.Save(
        SettingsDraft(
            forwardingEnabled = forwardingEnabled,
            cancelNotificationOnSuccess = cancelNotificationOnSuccess,
            barkServerUrl = barkServerUrl,
            barkDeviceKey = barkDeviceKey,
            barkDeviceKeys = barkDeviceKeys,
            barkLevel = barkLevel,
            barkVolume = barkVolume,
            barkBadge = barkBadge,
            barkCall = barkCall,
            barkAutoCopy = barkAutoCopy,
            barkCopy = barkCopy,
            barkSound = barkSound,
            barkIcon = barkIcon,
            barkImage = barkImage,
            barkGroup = barkGroup,
            barkCiphertext = barkCiphertext,
            barkIsArchive = barkIsArchive,
            barkUrl = barkUrl,
            barkAction = barkAction,
            barkNotificationId = barkNotificationId,
            barkDelete = barkDelete,
            barkUseMarkdown = barkUseMarkdown,
            allowedPackages = allowedPackages,
            blockedPackages = blockedPackages,
            keywordWhitelist = whitelist,
            keywordBlacklist = blacklist,
            dedupeSeconds = dedupeSeconds,
            filtersEnabled = filtersEnabled,
            excludeSystem = excludeSystem,
            excludeOngoing = excludeOngoing,
            excludeEmpty = excludeEmpty,
            autoRetry = autoRetry,
        )
    ))
}

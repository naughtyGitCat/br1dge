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
import com.example.notifybridge.domain.model.FilterRuleSet

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
    var webhookUrl by remember(uiState.settings.webhookUrl) { mutableStateOf(uiState.settings.webhookUrl) }
    var token by remember(uiState.settings.bearerToken) { mutableStateOf(uiState.settings.bearerToken) }
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
                                webhookUrl = webhookUrl,
                                token = token,
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
                                webhookUrl = webhookUrl,
                                token = token,
                                allowedPackages = allowedPackages,
                                blockedPackages = blockedPackages,
                                whitelist = whitelist,
                                blacklist = blacklist,
                                dedupeSeconds = dedupeSeconds,
                                filtersEnabled = it,
                            )
                        }
                    )
                    OutlinedTextField(value = webhookUrl, onValueChange = { webhookUrl = it }, label = { Text("Webhook URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text("Bearer Token") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = allowedPackages, onValueChange = { allowedPackages = it }, label = { Text("允许应用包名（逗号分隔）") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = blockedPackages, onValueChange = { blockedPackages = it }, label = { Text("黑名单应用包名（逗号分隔）") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = whitelist, onValueChange = { whitelist = it }, label = { Text("关键词白名单（逗号分隔）") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = blacklist, onValueChange = { blacklist = it }, label = { Text("关键词黑名单（逗号分隔）") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dedupeSeconds, onValueChange = { dedupeSeconds = it }, label = { Text("去重秒数") }, modifier = Modifier.fillMaxWidth())
                    SwitchRow(
                        title = "排除系统通知",
                        checked = uiState.settings.filterRuleSet.excludeSystemNotifications,
                        onCheckedChange = {
                            save(current = uiState.settings, onAction = onAction, webhookUrl = webhookUrl, token = token, allowedPackages = allowedPackages, blockedPackages = blockedPackages, whitelist = whitelist, blacklist = blacklist, dedupeSeconds = dedupeSeconds, excludeSystem = it)
                        }
                    )
                    SwitchRow(
                        title = "排除 ongoing 通知",
                        checked = uiState.settings.filterRuleSet.excludeOngoingNotifications,
                        onCheckedChange = {
                            save(current = uiState.settings, onAction = onAction, webhookUrl = webhookUrl, token = token, allowedPackages = allowedPackages, blockedPackages = blockedPackages, whitelist = whitelist, blacklist = blacklist, dedupeSeconds = dedupeSeconds, excludeOngoing = it)
                        }
                    )
                    SwitchRow(
                        title = "排除空正文",
                        checked = uiState.settings.filterRuleSet.excludeEmptyTextNotifications,
                        onCheckedChange = {
                            save(current = uiState.settings, onAction = onAction, webhookUrl = webhookUrl, token = token, allowedPackages = allowedPackages, blockedPackages = blockedPackages, whitelist = whitelist, blacklist = blacklist, dedupeSeconds = dedupeSeconds, excludeEmpty = it)
                        }
                    )
                    SwitchRow(
                        title = "自动重试",
                        checked = uiState.settings.filterRuleSet.autoRetryEnabled,
                        onCheckedChange = {
                            save(current = uiState.settings, onAction = onAction, webhookUrl = webhookUrl, token = token, allowedPackages = allowedPackages, blockedPackages = blockedPackages, whitelist = whitelist, blacklist = blacklist, dedupeSeconds = dedupeSeconds, autoRetry = it)
                        }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = {
                            save(uiState.settings, onAction, webhookUrl, token, allowedPackages, blockedPackages, whitelist, blacklist, dedupeSeconds)
                        }, modifier = Modifier.weight(1f)) {
                            Text("保存设置")
                        }
                        Button(onClick = { onAction(SettingsAction.TestSend) }, modifier = Modifier.weight(1f)) {
                            Text("测试发送")
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
    webhookUrl: String,
    token: String,
    allowedPackages: String,
    blockedPackages: String,
    whitelist: String,
    blacklist: String,
    dedupeSeconds: String,
    forwardingEnabled: Boolean = current.forwardingEnabled,
    filtersEnabled: Boolean = current.filterRuleSet.enabled,
    excludeSystem: Boolean = current.filterRuleSet.excludeSystemNotifications,
    excludeOngoing: Boolean = current.filterRuleSet.excludeOngoingNotifications,
    excludeEmpty: Boolean = current.filterRuleSet.excludeEmptyTextNotifications,
    autoRetry: Boolean = current.filterRuleSet.autoRetryEnabled,
) {
    onAction(
        SettingsAction.Save(
            current.copy(
                forwardingEnabled = forwardingEnabled,
                webhookUrl = webhookUrl.trim(),
                bearerToken = token.trim(),
                filterRuleSet = FilterRuleSet(
                    enabled = filtersEnabled,
                    allowedPackages = allowedPackages.splitToSet(),
                    blockedPackages = blockedPackages.splitToSet(),
                    keywordWhitelist = whitelist.splitToSet(),
                    keywordBlacklist = blacklist.splitToSet(),
                    excludeSystemNotifications = excludeSystem,
                    excludeOngoingNotifications = excludeOngoing,
                    excludeEmptyTextNotifications = excludeEmpty,
                    dedupeWindowSeconds = dedupeSeconds.toIntOrNull() ?: current.filterRuleSet.dedupeWindowSeconds,
                    autoRetryEnabled = autoRetry,
                )
            )
        )
    )
}

private fun String.splitToSet(): Set<String> = split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

package com.example.notifybridge.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifybridge.domain.model.AppSettings
import com.example.notifybridge.domain.model.BarkGroupMode
import com.example.notifybridge.system.util.InstalledAppInfo
import uk.deprecated.notifybridge.R

private const val BarkSoundDefaultValue = ""

private val barkLevelOptions = listOf(
    "active" to "active",
    "timeSensitive" to "timeSensitive",
    "passive" to "passive",
    "critical" to "critical",
)

private val barkActionOptions = listOf(
    "" to "settings_option_none",
    "alert" to "alert",
)

private val barkGroupModeOptions = listOf(
    BarkGroupMode.APP_NAME.name to "settings_group_app_name",
    BarkGroupMode.DEVICE_NAME.name to "settings_group_device_name",
    BarkGroupMode.APP_NAME_AT_DEVICE_NAME.name to "settings_group_app_at_device",
    BarkGroupMode.CUSTOM.name to "settings_group_custom",
)

private val barkSoundOptions = listOf(
    BarkSoundDefaultValue to "settings_sound_default",
    "alarm.caf" to "alarm.caf",
    "anticipate.caf" to "anticipate.caf",
    "bell.caf" to "bell.caf",
    "birdsong.caf" to "birdsong.caf",
    "bloom.caf" to "bloom.caf",
    "calypso.caf" to "calypso.caf",
    "chime.caf" to "chime.caf",
    "choo.caf" to "choo.caf",
    "descent.caf" to "descent.caf",
    "electronic.caf" to "electronic.caf",
    "fanfare.caf" to "fanfare.caf",
    "glass.caf" to "glass.caf",
    "gotosleep.caf" to "gotosleep.caf",
    "healthnotification.caf" to "healthnotification.caf",
    "horn.caf" to "horn.caf",
    "ladder.caf" to "ladder.caf",
    "mailsent.caf" to "mailsent.caf",
    "minuet.caf" to "minuet.caf",
    "multiwayinvitation.caf" to "multiwayinvitation.caf",
    "newmail.caf" to "newmail.caf",
    "newsflash.caf" to "newsflash.caf",
    "noir.caf" to "noir.caf",
    "paymentsuccess.caf" to "paymentsuccess.caf",
    "shake.caf" to "shake.caf",
    "sherwoodforest.caf" to "sherwoodforest.caf",
    "silence.caf" to "silence.caf",
    "spell.caf" to "spell.caf",
    "suspense.caf" to "suspense.caf",
    "telegraph.caf" to "telegraph.caf",
    "tiptoes.caf" to "tiptoes.caf",
    "typewriters.caf" to "typewriters.caf",
    "update.caf" to "update.caf",
)

private enum class AppForwardMode {
    DEFAULT,
    FORWARD,
    BLOCK,
}

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
    var barkGroupMode by remember(uiState.settings.barkGroupMode) { mutableStateOf(uiState.settings.barkGroupMode) }
    var barkGroupCustom by remember(uiState.settings.barkGroupCustom) { mutableStateOf(uiState.settings.barkGroupCustom) }
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
    var appSearchQuery by remember { mutableStateOf("") }

    val allowedPackageSet = remember(allowedPackages) { allowedPackages.splitToSet() }
    val blockedPackageSet = remember(blockedPackages) { blockedPackages.splitToSet() }
    val filteredApps = remember(uiState.installedApps, appSearchQuery) {
        val query = appSearchQuery.trim()
        if (query.isBlank()) {
            uiState.installedApps
        } else {
            uiState.installedApps.filter { app ->
                app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
            }
        }
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
        barkGroupMode = barkGroupMode,
        barkGroupCustom = barkGroupCustom,
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
                    Text(stringResource(R.string.settings_basic))
                    SwitchRow(
                        title = stringResource(R.string.settings_forwarding_enabled),
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
                        title = stringResource(R.string.settings_filters_enabled),
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
                        title = stringResource(R.string.settings_clear_after_success),
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
                    OutlinedTextField(
                        value = barkServerUrl,
                        onValueChange = { barkServerUrl = it },
                        label = { Text(stringResource(R.string.settings_bark_server_url)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    uiState.validation.barkServerUrlError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    OutlinedTextField(
                        value = barkDeviceKey,
                        onValueChange = { barkDeviceKey = it },
                        label = { Text(stringResource(R.string.settings_bark_device_key)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    uiState.validation.barkDeviceKeyError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.settings_app_rules))
                    Text(
                        text = stringResource(
                            R.string.settings_app_rules_summary,
                            allowedPackageSet.size,
                            blockedPackageSet.size,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = appSearchQuery,
                        onValueChange = { appSearchQuery = it },
                        label = { Text(stringResource(R.string.settings_app_search)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        items(filteredApps, key = { it.packageName }) { app ->
            AppRuleCard(
                app = app,
                mode = app.forwardMode(allowedPackageSet, blockedPackageSet),
                onModeSelected = { mode ->
                    val (updatedAllowed, updatedBlocked) = updatePackageFilters(
                        packageName = app.packageName,
                        mode = mode,
                        allowedPackages = allowedPackages,
                        blockedPackages = blockedPackages,
                    )
                    allowedPackages = updatedAllowed
                    blockedPackages = updatedBlocked
                },
            )
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.settings_bark_advanced))
                    OutlinedTextField(
                        value = barkDeviceKeys,
                        onValueChange = { barkDeviceKeys = it },
                        label = { Text(stringResource(R.string.settings_bark_device_keys)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SelectionField(
                        label = stringResource(R.string.settings_bark_level),
                        value = barkLevel,
                        options = barkLevelOptions,
                        onValueSelected = { barkLevel = it },
                    )
                    uiState.validation.barkLevelError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    OutlinedTextField(
                        value = barkVolume,
                        onValueChange = { barkVolume = it },
                        label = { Text(stringResource(R.string.settings_bark_volume)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    uiState.validation.barkVolumeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    OutlinedTextField(
                        value = barkBadge,
                        onValueChange = { barkBadge = it },
                        label = { Text(stringResource(R.string.settings_bark_badge)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    uiState.validation.barkBadgeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    OutlinedTextField(
                        value = barkCopy,
                        onValueChange = { barkCopy = it },
                        label = { Text(stringResource(R.string.settings_bark_copy)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SelectionField(
                        label = stringResource(R.string.settings_bark_sound),
                        value = barkSound,
                        options = barkSoundOptions.withCurrentValue(barkSound),
                        onValueSelected = { barkSound = it },
                    )
                    OutlinedTextField(
                        value = barkIcon,
                        onValueChange = { barkIcon = it },
                        label = { Text(stringResource(R.string.settings_bark_icon)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    uiState.validation.barkIconError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    OutlinedTextField(
                        value = barkImage,
                        onValueChange = { barkImage = it },
                        label = { Text(stringResource(R.string.settings_bark_image)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    uiState.validation.barkImageError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    SelectionField(
                        label = stringResource(R.string.settings_bark_group),
                        value = barkGroupMode.name,
                        options = barkGroupModeOptions,
                        onValueSelected = { barkGroupMode = BarkGroupMode.valueOf(it) },
                    )
                    if (barkGroupMode == BarkGroupMode.CUSTOM) {
                        OutlinedTextField(
                            value = barkGroupCustom,
                            onValueChange = { barkGroupCustom = it },
                            label = { Text(stringResource(R.string.settings_bark_group_custom)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    OutlinedTextField(
                        value = barkCiphertext,
                        onValueChange = { barkCiphertext = it },
                        label = { Text(stringResource(R.string.settings_bark_ciphertext)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = barkUrl,
                        onValueChange = { barkUrl = it },
                        label = { Text(stringResource(R.string.settings_bark_jump_url)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    uiState.validation.barkUrlError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    SelectionField(
                        label = stringResource(R.string.settings_bark_action),
                        value = barkAction,
                        options = barkActionOptions,
                        onValueSelected = { barkAction = it },
                    )
                    uiState.validation.barkActionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    OutlinedTextField(
                        value = barkNotificationId,
                        onValueChange = { barkNotificationId = it },
                        label = { Text(stringResource(R.string.settings_bark_notification_id)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SwitchRow(title = stringResource(R.string.settings_bark_call), checked = barkCall, onCheckedChange = { barkCall = it })
                    SwitchRow(title = stringResource(R.string.settings_bark_auto_copy), checked = barkAutoCopy, onCheckedChange = { barkAutoCopy = it })
                    SwitchRow(title = stringResource(R.string.settings_bark_markdown), checked = barkUseMarkdown, onCheckedChange = { barkUseMarkdown = it })
                    SwitchRow(title = stringResource(R.string.settings_bark_archive), checked = barkIsArchive, onCheckedChange = { barkIsArchive = it })
                    SwitchRow(title = stringResource(R.string.settings_bark_delete), checked = barkDelete, onCheckedChange = { barkDelete = it })
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.settings_manual_filters))
                    OutlinedTextField(
                        value = allowedPackages,
                        onValueChange = { allowedPackages = it },
                        label = { Text(stringResource(R.string.settings_allowed_packages)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = blockedPackages,
                        onValueChange = { blockedPackages = it },
                        label = { Text(stringResource(R.string.settings_blocked_packages)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = whitelist,
                        onValueChange = { whitelist = it },
                        label = { Text(stringResource(R.string.settings_keyword_whitelist)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = blacklist,
                        onValueChange = { blacklist = it },
                        label = { Text(stringResource(R.string.settings_keyword_blacklist)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = dedupeSeconds,
                        onValueChange = { dedupeSeconds = it },
                        label = { Text(stringResource(R.string.settings_dedupe_seconds)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    uiState.validation.dedupeSecondsError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    SwitchRow(
                        title = stringResource(R.string.settings_exclude_system),
                        checked = uiState.settings.filterRuleSet.excludeSystemNotifications,
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
                                excludeSystem = it,
                            )
                        }
                    )
                    SwitchRow(
                        title = stringResource(R.string.settings_exclude_ongoing),
                        checked = uiState.settings.filterRuleSet.excludeOngoingNotifications,
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
                                excludeOngoing = it,
                            )
                        }
                    )
                    SwitchRow(
                        title = stringResource(R.string.settings_exclude_empty),
                        checked = uiState.settings.filterRuleSet.excludeEmptyTextNotifications,
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
                                excludeEmpty = it,
                            )
                        }
                    )
                    SwitchRow(
                        title = stringResource(R.string.settings_auto_retry),
                        checked = uiState.settings.filterRuleSet.autoRetryEnabled,
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
                                autoRetry = it,
                            )
                        }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { onAction(SettingsAction.Save(draft)) }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_save))
                        }
                        Button(
                            onClick = { onAction(SettingsAction.TestSend(draft)) },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.testSendState !is TestSendState.Running,
                        ) {
                            Text(stringResource(if (uiState.testSendState is TestSendState.Running) R.string.settings_testing else R.string.settings_test_send))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { onAction(SettingsAction.ClearLogs) }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_clear_logs))
                        }
                        Button(onClick = { onAction(SettingsAction.ExportDebug) }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_export_debug))
                        }
                    }
                    uiState.message?.let { Text(it) }
                    when (val state = uiState.testSendState) {
                        TestSendState.Idle -> Unit
                        TestSendState.Running -> Text(stringResource(R.string.settings_testing_message))
                        is TestSendState.Success -> Text(stringResource(R.string.settings_connectivity_result, state.message))
                        is TestSendState.Failure -> Text(stringResource(R.string.settings_connectivity_result, state.message))
                    }
                    uiState.exportingPath?.let { Text(stringResource(R.string.settings_export_path, it)) }
                }
            }
        }
    }
}

@Composable
private fun AppRuleCard(
    app: InstalledAppInfo,
    mode: AppForwardMode,
    onModeSelected: (AppForwardMode) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(app.appName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (app.isSystemApp) {
                    Text(
                        text = stringResource(R.string.settings_app_system),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = mode == AppForwardMode.FORWARD,
                    onClick = { onModeSelected(AppForwardMode.FORWARD) },
                    label = { Text(stringResource(R.string.settings_app_mode_forward)) },
                )
                FilterChip(
                    selected = mode == AppForwardMode.BLOCK,
                    onClick = { onModeSelected(AppForwardMode.BLOCK) },
                    label = { Text(stringResource(R.string.settings_app_mode_block)) },
                )
                FilterChip(
                    selected = mode == AppForwardMode.DEFAULT,
                    onClick = { onModeSelected(AppForwardMode.DEFAULT) },
                    label = { Text(stringResource(R.string.settings_app_mode_default)) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionField(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onValueSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = (options.firstOrNull { it.first == value }?.second ?: value).asLocalizedOption()
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (optionValue, optionLabel) ->
                DropdownMenuItem(
                    text = { Text(optionLabel.asLocalizedOption()) },
                    onClick = {
                        onValueSelected(optionValue)
                        expanded = false
                    },
                )
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

private fun InstalledAppInfo.forwardMode(
    allowedPackages: Set<String>,
    blockedPackages: Set<String>,
): AppForwardMode = when {
    packageName in blockedPackages -> AppForwardMode.BLOCK
    packageName in allowedPackages -> AppForwardMode.FORWARD
    else -> AppForwardMode.DEFAULT
}

private fun updatePackageFilters(
    packageName: String,
    mode: AppForwardMode,
    allowedPackages: String,
    blockedPackages: String,
): Pair<String, String> {
    val allowed = allowedPackages.splitToSet().toMutableSet()
    val blocked = blockedPackages.splitToSet().toMutableSet()
    allowed.remove(packageName)
    blocked.remove(packageName)
    when (mode) {
        AppForwardMode.DEFAULT -> Unit
        AppForwardMode.FORWARD -> allowed += packageName
        AppForwardMode.BLOCK -> blocked += packageName
    }
    return allowed.joinToString(",") to blocked.joinToString(",")
}

private fun String.splitToSet(): Set<String> = split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .toSet()

private fun List<Pair<String, String>>.withCurrentValue(current: String): List<Pair<String, String>> {
    if (current.isBlank() || any { it.first == current }) return this
    return this + listOf(current to current)
}

@Composable
private fun String.asLocalizedOption(): String = when (this) {
    "settings_option_none" -> stringResource(R.string.settings_option_none)
    "settings_group_app_name" -> stringResource(R.string.settings_group_app_name)
    "settings_group_device_name" -> stringResource(R.string.settings_group_device_name)
    "settings_group_app_at_device" -> stringResource(R.string.settings_group_app_at_device)
    "settings_group_custom" -> stringResource(R.string.settings_group_custom)
    "settings_sound_default" -> stringResource(R.string.settings_sound_default)
    else -> this
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
    barkGroupMode: BarkGroupMode = current.barkGroupMode,
    barkGroupCustom: String = current.barkGroupCustom,
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
    onAction(
        SettingsAction.Save(
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
                barkGroupMode = barkGroupMode,
                barkGroupCustom = barkGroupCustom,
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
        )
    )
}

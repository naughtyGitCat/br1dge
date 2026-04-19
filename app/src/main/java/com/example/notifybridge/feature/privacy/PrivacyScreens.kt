package com.example.notifybridge.feature.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uk.deprecated.notifybridge.R

@Composable
fun PrivacyDisclosureScreen(
    onAccept: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.privacy_disclosure_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.privacy_disclosure_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.privacy_disclosure_points_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                PolicyBullet(text = stringResource(R.string.privacy_disclosure_point_notifications))
                PolicyBullet(text = stringResource(R.string.privacy_disclosure_point_apps))
                PolicyBullet(text = stringResource(R.string.privacy_disclosure_point_transfer))
                PolicyBullet(text = stringResource(R.string.privacy_disclosure_point_control))
            }
        }
        OutlinedButton(
            onClick = onOpenPrivacyPolicy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.privacy_disclosure_policy))
        }
        Button(
            onClick = onAccept,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.privacy_disclosure_agree))
        }
    }
}

@Composable
fun PrivacyPolicyScreenRoute(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
) {
    PrivacyPolicyScreen(
        contentPadding = contentPadding,
        onBack = onBack,
    )
}

@Composable
private fun PrivacyPolicyScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.detail_back))
        }
        Text(
            text = stringResource(R.string.privacy_policy_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.privacy_policy_summary),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PolicySection(
            title = stringResource(R.string.privacy_policy_section_data_title),
            body = stringResource(R.string.privacy_policy_section_data_body),
        )
        PolicySection(
            title = stringResource(R.string.privacy_policy_section_usage_title),
            body = stringResource(R.string.privacy_policy_section_usage_body),
        )
        PolicySection(
            title = stringResource(R.string.privacy_policy_section_sharing_title),
            body = stringResource(R.string.privacy_policy_section_sharing_body),
        )
        PolicySection(
            title = stringResource(R.string.privacy_policy_section_storage_title),
            body = stringResource(R.string.privacy_policy_section_storage_body),
        )
        PolicySection(
            title = stringResource(R.string.privacy_policy_section_controls_title),
            body = stringResource(R.string.privacy_policy_section_controls_body),
        )
        PolicySection(
            title = stringResource(R.string.privacy_policy_section_contact_title),
            body = stringResource(R.string.privacy_policy_section_contact_body),
        )
    }
}

@Composable
private fun PolicySection(
    title: String,
    body: String,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PolicyBullet(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

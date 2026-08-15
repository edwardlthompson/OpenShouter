package org.openshouter.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.domain.MessageChannelPolicy
import org.openshouter.domain.TtsFormat

@Composable
fun MessageChannelScreen(
    policy: MessageChannelPolicy,
    format: String,
    onPolicy: (MessageChannelPolicy) -> Unit,
    onFormat: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text("Message shout", style = MaterialTheme.typography.headlineSmall)
        ToggleRow("Enable message channel", policy.enabled) { onPolicy(policy.copy(enabled = it)) }
        ToggleRow("Read unknown senders", policy.speakUnknown) { onPolicy(policy.copy(speakUnknown = it)) }
        ToggleRow("Read message body", policy.speakBody) { onPolicy(policy.copy(speakBody = it)) }
        ToggleRow("Known contacts only", policy.knownContactsOnly) {
            onPolicy(policy.copy(knownContactsOnly = it))
        }
        OutlinedTextField(
            value = format,
            onValueChange = onFormat,
            label = { Text("Format (${TtsFormat.MESSAGE_DEFAULT})") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) { Text("Close") }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            modifier = Modifier.semantics { contentDescription = label },
        )
    }
}

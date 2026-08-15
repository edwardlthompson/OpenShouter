package org.openshouter.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import dev.foss.goldenpath.ui.insets.bottomInsetPadding
import dev.foss.goldenpath.ui.theme.SpacingMd
import org.openshouter.domain.ContactRule

@Composable
fun ContactRulesScreen(
    rule: ContactRule,
    speakUnknownCall: Boolean,
    speakUnknownMessage: Boolean,
    onRuleChange: (ContactRule) -> Unit,
    onSpeakUnknownCall: (Boolean) -> Unit,
    onSpeakUnknownMessage: (Boolean) -> Unit,
    callFormat: String = "",
    onCallFormat: (String) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var nickNumber by remember { mutableStateOf("") }
    var nickDisplay by remember { mutableStateOf("") }
    var blockNumber by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(SpacingMd),
        verticalArrangement = Arrangement.spacedBy(SpacingMd),
    ) {
        Text("Contacts", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Nicknames and a blacklist change what is spoken. Counts only — numbers stay off the list.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = nickNumber,
            onValueChange = { nickNumber = it },
            label = { Text("Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
        )
        OutlinedTextField(
            value = nickDisplay,
            onValueChange = { nickDisplay = it.take(ContactRule.MAX_NICK) },
            label = { Text("Nickname") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(
            onClick = {
                val next = ContactRules.addNick(rule, nickNumber, nickDisplay) ?: return@Button
                nickNumber = ""
                nickDisplay = ""
                onRuleChange(next)
            },
        ) {
            Text("Add nickname")
        }
        OutlinedTextField(
            value = blockNumber,
            onValueChange = { blockNumber = it },
            label = { Text("Blacklist number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
        )
        Button(
            onClick = {
                val next = ContactRules.addBlock(rule, blockNumber) ?: return@Button
                blockNumber = ""
                onRuleChange(next)
            },
        ) {
            Text("Add to blacklist")
        }
        Text("Nicknames: ${rule.nicknames.size}", style = MaterialTheme.typography.titleMedium)
        Text("Blocked: ${rule.blacklist.size}", style = MaterialTheme.typography.titleMedium)
        ToggleRow("Speak unknown callers", speakUnknownCall, onSpeakUnknownCall)
        ToggleRow("Speak unknown messages", speakUnknownMessage, onSpeakUnknownMessage)
        OutlinedTextField(
            value = callFormat,
            onValueChange = onCallFormat,
            label = { Text("Call format (%name %number)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(onClick = onBack, modifier = Modifier.bottomInsetPadding()) {
            Text("Close")
        }
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

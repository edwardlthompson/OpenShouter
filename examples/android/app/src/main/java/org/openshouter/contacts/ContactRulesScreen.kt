package org.openshouter.contacts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import dev.foss.goldenpath.R
import org.openshouter.domain.ContactRule
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

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
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    var nickNumber by remember { mutableStateOf("") }
    var nickDisplay by remember { mutableStateOf("") }
    var blockNumber by remember { mutableStateOf("") }
    MenuScaffold(stringResource(R.string.nav_contacts), scrollStore, "contacts", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_actions)) {
            MenuBody {
                Text(stringResource(R.string.contacts_help), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = nickNumber,
                    onValueChange = { nickNumber = it },
                    label = { Text(stringResource(R.string.contacts_number)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = nickDisplay,
                    onValueChange = { nickDisplay = it.take(ContactRule.MAX_NICK) },
                    label = { Text(stringResource(R.string.contacts_nickname)) },
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
                ) { Text(stringResource(R.string.contacts_add_nick)) }
                OutlinedTextField(
                    value = blockNumber,
                    onValueChange = { blockNumber = it },
                    label = { Text(stringResource(R.string.contacts_block)) },
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
                ) { Text(stringResource(R.string.contacts_add_block)) }
                Text(stringResource(R.string.contacts_nicks, rule.nicknames.size))
                Text(stringResource(R.string.contacts_blocked, rule.blacklist.size))
                OutlinedTextField(
                    value = callFormat,
                    onValueChange = onCallFormat,
                    label = { Text(stringResource(R.string.contacts_call_format)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            MenuToggle(stringResource(R.string.contacts_unknown_call), speakUnknownCall, onSpeakUnknownCall, true)
            MenuToggle(stringResource(R.string.contacts_unknown_message), speakUnknownMessage, onSpeakUnknownMessage, true)
        }
    }
}

package org.openshouter.message

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import org.openshouter.domain.MessageChannelPolicy
import org.openshouter.ui.menu.MenuBody
import org.openshouter.ui.menu.MenuScaffold
import org.openshouter.ui.menu.MenuScrollStore
import org.openshouter.ui.menu.MenuSection
import org.openshouter.ui.menu.MenuToggle

@Composable
fun MessageChannelScreen(
    policy: MessageChannelPolicy,
    format: String,
    onPolicy: (MessageChannelPolicy) -> Unit,
    onFormat: (String) -> Unit,
    onBack: () -> Unit,
    scrollStore: MenuScrollStore,
    modifier: Modifier = Modifier,
) {
    MenuScaffold(stringResource(R.string.nav_messages), scrollStore, "messages", onBack, modifier) {
        MenuSection(stringResource(R.string.menu_section_shout)) {
            MenuToggle(stringResource(R.string.messages_enable), policy.enabled, {
                onPolicy(policy.copy(enabled = it))
            })
            MenuToggle(stringResource(R.string.messages_unknown), policy.speakUnknown, { onPolicy(policy.copy(speakUnknown = it)) }, true)
            MenuToggle(stringResource(R.string.messages_body), policy.speakBody, { onPolicy(policy.copy(speakBody = it)) }, true)
            MenuToggle(stringResource(R.string.messages_known), policy.knownContactsOnly, {
                onPolicy(policy.copy(knownContactsOnly = it))
            }, true)
            MenuBody {
                OutlinedTextField(
                    value = format,
                    onValueChange = onFormat,
                    label = { Text(stringResource(R.string.messages_format)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
    }
}

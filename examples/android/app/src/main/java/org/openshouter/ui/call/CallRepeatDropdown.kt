package org.openshouter.ui.call

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import org.openshouter.domain.CallRepeatMode
import org.openshouter.ui.menu.MenuDropdown

@Composable
fun CallRepeatDropdown(
    mode: CallRepeatMode,
    onChange: (CallRepeatMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val once = stringResource(R.string.apps_call_repeat_once)
    val until = stringResource(R.string.apps_call_repeat_until)
    val off = stringResource(R.string.apps_call_repeat_off)
    val text = when (mode) {
        CallRepeatMode.ONCE -> once
        CallRepeatMode.UNTIL_ANSWERED -> until
        CallRepeatMode.OFF -> off
    }
    MenuDropdown(
        label = stringResource(R.string.apps_call_repeat_label),
        text = text,
        options = listOf(
            CallRepeatMode.ONCE.name to once,
            CallRepeatMode.UNTIL_ANSWERED.name to until,
            CallRepeatMode.OFF.name to off,
        ),
        onSelect = { id ->
            runCatching { CallRepeatMode.valueOf(id) }.getOrNull()?.let(onChange)
        },
        modifier = modifier,
    )
}

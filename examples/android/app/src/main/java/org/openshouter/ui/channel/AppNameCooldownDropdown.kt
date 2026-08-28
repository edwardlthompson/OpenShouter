package org.openshouter.ui.channel

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import org.openshouter.domain.AppNameCooldown
import org.openshouter.ui.menu.MenuDropdown

@Composable
fun AppNameCooldownDropdown(
    seconds: Int,
    modifier: Modifier = Modifier,
    onChange: (Int) -> Unit,
) {
    val options = AppNameCooldown.OPTIONS_SECONDS.map { it.toString() to cooldownLabel(it) }
    val current = AppNameCooldown.clampSeconds(seconds)
    MenuDropdown(
        label = stringResource(R.string.channel_app_name_cooldown),
        text = cooldownLabel(current),
        options = options,
        onSelect = { id -> onChange(id.toIntOrNull() ?: AppNameCooldown.DEFAULT_SECONDS) },
        modifier = modifier,
    )
}

@Composable
private fun cooldownLabel(seconds: Int): String = when (seconds) {
    0 -> stringResource(R.string.channel_app_name_cooldown_off)
    60 -> stringResource(R.string.channel_app_name_cooldown_minute)
    in 61..Int.MAX_VALUE -> stringResource(R.string.channel_app_name_cooldown_minutes, seconds / 60)
    else -> stringResource(R.string.channel_app_name_cooldown_seconds, seconds)
}

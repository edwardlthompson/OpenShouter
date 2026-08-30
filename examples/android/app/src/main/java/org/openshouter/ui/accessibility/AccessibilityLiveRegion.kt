package org.openshouter.ui.accessibility

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

@Composable
fun SpokenLiveRegion(
    lastUtterance: String,
    modifier: Modifier = Modifier,
) {
    if (lastUtterance.isNotBlank()) {
        Box(
            modifier = modifier.semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = lastUtterance
            },
        ) {
            Text(text = lastUtterance)
        }
    }
}

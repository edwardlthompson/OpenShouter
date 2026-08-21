package org.openshouter.ui.history

import dev.foss.goldenpath.R
import org.openshouter.domain.IgnoreReason

internal fun ignoreReasonLabel(raw: String): Int? {
    val reason = runCatching { IgnoreReason.valueOf(raw) }.getOrNull() ?: return null
    return when (reason) {
        IgnoreReason.NONE -> null
        IgnoreReason.EMPTY -> R.string.history_reason_empty
        IgnoreReason.GROUP -> R.string.history_reason_group
        IgnoreReason.REPEAT -> R.string.history_reason_repeat
        IgnoreReason.FILTER -> R.string.history_reason_filter
        IgnoreReason.GATE,
        IgnoreReason.GATE_MASTER,
        IgnoreReason.GATE_PLACE,
        IgnoreReason.GATE_QUIET,
        IgnoreReason.GATE_SCREEN,
        IgnoreReason.GATE_HEADSET,
        IgnoreReason.GATE_SILENT,
        IgnoreReason.GATE_CALL,
        -> R.string.history_reason_gate
        IgnoreReason.IMPORTANCE -> R.string.history_reason_importance
    }
}

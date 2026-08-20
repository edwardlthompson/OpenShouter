package org.openshouter.display

data class DisplayModeChoice(
    val modeId: Int,
    val width: Int,
    val height: Int,
    val refreshHz: Float,
)

fun fastestSameResolutionModeId(
    modes: List<DisplayModeChoice>,
    current: DisplayModeChoice,
): Int? = modes
    .filter { it.width == current.width && it.height == current.height }
    .maxByOrNull { it.refreshHz }
    ?.modeId

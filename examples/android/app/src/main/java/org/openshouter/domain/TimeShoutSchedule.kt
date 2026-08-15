package org.openshouter.domain

data class TimeShoutSchedule(
    val enabled: Boolean = false,
    val intervalMinutes: Int = TimeShout.INTERVAL_HOUR,
    val exact: Boolean = true,
) {
    fun normalized(): TimeShoutSchedule =
        copy(intervalMinutes = TimeShout.normalizeInterval(intervalMinutes))
}

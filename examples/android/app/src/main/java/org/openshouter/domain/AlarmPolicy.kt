package org.openshouter.domain

object AlarmPolicy {
    fun useExact(wantExact: Boolean, canScheduleExact: Boolean): Boolean =
        wantExact && canScheduleExact
}

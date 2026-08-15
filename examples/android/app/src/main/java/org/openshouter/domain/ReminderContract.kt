package org.openshouter.domain

object ReminderContract {
    const val ACTION = "org.openshouter.action.REMINDER_FIRE"
    const val EXTRA_ID = "reminder_id"
    const val EXTRA_ALSO_NOTIFY = "also_notify"
    const val REQUEST_BASE = 7100

    fun requestCode(id: Long): Int = REQUEST_BASE + (id.mod(1_000L)).toInt()

    fun validId(id: Long): Boolean = id > 0
}

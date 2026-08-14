package org.openshouter.call

import android.annotation.SuppressLint
import android.content.Context
import android.provider.CallLog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.openshouter.domain.CallNumberResolver

@Singleton
class CallLogLookup @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun resolve(hint: String): String = CallNumberResolver.prefer(hint, lastRecentIncoming())

    @SuppressLint("MissingPermission")
    private fun lastRecentIncoming(): String? {
        val cutoff = System.currentTimeMillis() - 20_000L
        val projection = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE)
        return runCatching {
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                "${CallLog.Calls.DATE} > ?",
                arrayOf(cutoff.toString()),
                "${CallLog.Calls.DATE} DESC",
            )?.use { cursor ->
                val numIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE)
                while (cursor.moveToNext()) {
                    val type = cursor.getInt(typeIdx)
                    if (type != CallLog.Calls.INCOMING_TYPE && type != CallLog.Calls.MISSED_TYPE) {
                        continue
                    }
                    val number = cursor.getString(numIdx)
                    if (!number.isNullOrBlank()) return@use number
                }
                null
            }
        }.getOrNull()
    }
}

package org.openshouter.call

import org.openshouter.data.HistoryDao
import org.openshouter.data.HistoryEntity
import org.openshouter.domain.SpokenEvent

object CallHistory {
    const val CELLULAR_PACKAGE = "com.android.phone"

    fun ringingRow(postedAt: Long, spoken: String) = HistoryEntity(
        postedAt = postedAt,
        packageName = CELLULAR_PACKAGE,
        title = "",
        text = "",
        spoken = spoken,
        kind = SpokenEvent.Kind.CALL.name,
    )

    suspend fun insertOnce(dao: HistoryDao, spoken: String) {
        dao.insert(ringingRow(System.currentTimeMillis(), spoken))
        dao.pruneTo(100)
    }
}

package org.openshouter.data

import org.openshouter.domain.ShoutHistory
import org.openshouter.domain.SpokenEvent

object ShoutHistoryStore {
    fun row(kind: SpokenEvent.Kind, spoken: String, postedAt: Long): HistoryEntity? {
        if (!ShoutHistory.records(kind) || spoken.isBlank()) return null
        return HistoryEntity(
            postedAt = postedAt,
            packageName = ShoutHistory.PACKAGE,
            title = "",
            text = "",
            spoken = spoken,
            kind = kind.name,
        )
    }

    suspend fun insertOnce(dao: HistoryDao, kind: SpokenEvent.Kind, spoken: String) {
        val entity = row(kind, spoken, System.currentTimeMillis()) ?: return
        dao.insert(entity)
        dao.pruneTo(100)
    }
}

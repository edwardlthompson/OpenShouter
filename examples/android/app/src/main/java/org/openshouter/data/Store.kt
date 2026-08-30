package org.openshouter.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.openshouter.domain.IgnoreReason

@Entity(tableName = "notification_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postedAt: Long,
    val packageName: String,
    val title: String,
    val text: String,
    val spoken: String,
    val ignoreReason: String = IgnoreReason.NONE.name,
    val channelId: String = "",
    val channelName: String = "",
    val kind: String = "NOTIFICATION",
)

@Entity(tableName = "regex_rules")
data class RegexEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String,
    val action: String,
    val replacement: String,
)

@Entity(tableName = "geo_places")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val silentInside: Boolean,
)

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(row: HistoryEntity)

    @Query("SELECT * FROM notification_history ORDER BY postedAt DESC LIMIT :limit")
    fun recent(limit: Int = 100): Flow<List<HistoryEntity>>

    @Query("DELETE FROM notification_history")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM notification_history")
    suspend fun count(): Int

    @Query(
        "SELECT MIN(postedAt) FROM notification_history WHERE id IN " +
            "(SELECT id FROM notification_history ORDER BY postedAt DESC LIMIT :keep)",
    )
    suspend fun minKeptPostedAt(keep: Int): Long?

    @Query("DELETE FROM notification_history WHERE postedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    suspend fun pruneTo(keep: Int = 100) {
        if (count() <= keep) return
        val cutoff = minKeptPostedAt(keep) ?: return
        deleteOlderThan(cutoff)
    }
}

@Dao
interface RegexDao {
    @Query("SELECT * FROM regex_rules")
    fun all(): Flow<List<RegexEntity>>

    @Query("SELECT * FROM regex_rules")
    suspend fun snapshot(): List<RegexEntity>

    @Insert
    suspend fun insert(row: RegexEntity)

    @Query("DELETE FROM regex_rules WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface PlaceDao {
    @Query("SELECT * FROM geo_places")
    fun all(): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM geo_places")
    suspend fun snapshot(): List<PlaceEntity>

    @Insert
    suspend fun insert(row: PlaceEntity)

    @Update
    suspend fun update(row: PlaceEntity)

    @Query("UPDATE geo_places SET label = :label WHERE id = :id")
    suspend fun rename(id: Long, label: String)

    @Query("UPDATE geo_places SET silentInside = :silent WHERE id = :id")
    suspend fun setSilent(id: Long, silent: Boolean)

    @Query("DELETE FROM geo_places WHERE id = :id")
    suspend fun delete(id: Long)
}

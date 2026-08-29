package org.openshouter.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sound_leaks", primaryKeys = ["packageName", "channelId"])
data class SoundLeakEntity(
    val packageName: String,
    val channelId: String,
    val channelName: String,
    val evidence: String,
    val lastSeen: Long,
)

@Dao
interface SoundLeakDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: SoundLeakEntity)

    @Query("SELECT * FROM sound_leaks ORDER BY lastSeen DESC LIMIT :limit")
    fun recent(limit: Int = 50): Flow<List<SoundLeakEntity>>

    @Query("SELECT COUNT(*) FROM sound_leaks")
    suspend fun count(): Int

    @Query("SELECT lastSeen FROM sound_leaks ORDER BY lastSeen DESC LIMIT 1 OFFSET :keep")
    suspend fun cutoffAfter(keep: Int): Long?

    @Query("DELETE FROM sound_leaks WHERE lastSeen < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("DELETE FROM sound_leaks WHERE packageName = :packageName AND channelId = :channelId")
    suspend fun delete(packageName: String, channelId: String)

    @Query("DELETE FROM sound_leaks WHERE evidence = :evidence")
    suspend fun deleteByEvidence(evidence: String)

    suspend fun pruneTo(keep: Int = 50) {
        if (count() <= keep) return
        val cutoff = cutoffAfter(keep) ?: return
        deleteOlderThan(cutoff)
    }
}

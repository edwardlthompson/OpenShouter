package org.openshouter.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val intervalMinutes: Int,
    val nextAtMillis: Long,
    val enabled: Boolean,
    val alsoNotify: Boolean,
) {
    companion object {
        const val MAX_TEXT = 200

        fun normalizeText(text: String): String? =
            text.trim().takeIf { it.isNotEmpty() }?.take(MAX_TEXT)
    }
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY nextAtMillis ASC")
    fun all(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE enabled = 1")
    suspend fun enabled(): List<ReminderEntity>

    @Insert
    suspend fun insert(row: ReminderEntity): Long

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: Long)
}

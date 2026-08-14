package org.openshouter.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openshouter.domain.AppSettings
import org.openshouter.domain.AppSpeakRule
import org.openshouter.domain.FilterMode

@Entity(tableName = "app_speak_rules")
data class AppSpeakEntity(
    @PrimaryKey val packageName: String,
    val speakAppName: Boolean,
    val speakNotification: Boolean,
)

@Dao
interface AppSpeakDao {
    @Query("SELECT * FROM app_speak_rules")
    fun all(): Flow<List<AppSpeakEntity>>

    @Query("SELECT * FROM app_speak_rules")
    suspend fun snapshot(): List<AppSpeakEntity>

    @Query("SELECT COUNT(*) FROM app_speak_rules")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: AppSpeakEntity)

    @Query("DELETE FROM app_speak_rules WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}

@Singleton
class AppSpeakStore @Inject constructor(
    private val dao: AppSpeakDao,
) {
    val rules: Flow<List<AppSpeakRule>> = dao.all().map { rows -> rows.map { it.toDomain() } }

    suspend fun snapshot(): Map<String, AppSpeakRule> =
        dao.snapshot().map { it.toDomain() }.associateBy { it.packageName }

    suspend fun set(packageName: String, speakAppName: Boolean, speakNotification: Boolean) {
        val pkg = packageName.trim()
        if (pkg.isEmpty()) return
        if (!speakAppName && !speakNotification) {
            dao.delete(pkg)
            return
        }
        dao.upsert(AppSpeakEntity(pkg, speakAppName, speakNotification))
    }

    suspend fun importWhitelist(settings: AppSettings) {
        if (dao.count() > 0) return
        if (settings.filterMode != FilterMode.WHITELIST) return
        for (pkg in settings.listedPackages) {
            val name = pkg.trim()
            if (name.isEmpty()) continue
            dao.upsert(AppSpeakEntity(name, speakAppName = true, speakNotification = true))
        }
    }
}

fun AppSpeakEntity.toDomain(): AppSpeakRule =
    AppSpeakRule(packageName, speakAppName, speakNotification)

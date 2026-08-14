package org.openshouter.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [HistoryEntity::class, RegexEntity::class, PlaceEntity::class, AppSpeakEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun history(): HistoryDao
    abstract fun regex(): RegexDao
    abstract fun places(): PlaceDao
    abstract fun appSpeak(): AppSpeakDao
}

val MIGRATION_1_2 = Migration(1, 2) { db: SupportSQLiteDatabase ->
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS app_speak_rules (" +
            "packageName TEXT NOT NULL PRIMARY KEY, " +
            "speakAppName INTEGER NOT NULL, " +
            "speakNotification INTEGER NOT NULL)",
    )
}

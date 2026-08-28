package org.openshouter.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        HistoryEntity::class,
        RegexEntity::class,
        PlaceEntity::class,
        AppSpeakEntity::class,
        ReminderEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun history(): HistoryDao
    abstract fun regex(): RegexDao
    abstract fun places(): PlaceDao
    abstract fun appSpeak(): AppSpeakDao
    abstract fun reminders(): ReminderDao
}

val MIGRATION_1_2 = Migration(1, 2) { db: SupportSQLiteDatabase ->
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS app_speak_rules (" +
            "packageName TEXT NOT NULL PRIMARY KEY, " +
            "speakAppName INTEGER NOT NULL, " +
            "speakNotification INTEGER NOT NULL)",
    )
}

val MIGRATION_2_3 = Migration(2, 3) { db: SupportSQLiteDatabase ->
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS reminders (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "text TEXT NOT NULL, " +
            "intervalMinutes INTEGER NOT NULL, " +
            "nextAtMillis INTEGER NOT NULL, " +
            "enabled INTEGER NOT NULL, " +
            "alsoNotify INTEGER NOT NULL)",
    )
}

val MIGRATION_3_4 = Migration(3, 4) { db: SupportSQLiteDatabase ->
    db.execSQL(
        "ALTER TABLE notification_history ADD COLUMN ignoreReason TEXT NOT NULL DEFAULT 'NONE'",
    )
}

val MIGRATION_4_5 = Migration(4, 5) { db: SupportSQLiteDatabase ->
    db.execSQL(
        "ALTER TABLE notification_history ADD COLUMN channelId TEXT NOT NULL DEFAULT ''",
    )
    db.execSQL(
        "ALTER TABLE notification_history ADD COLUMN channelName TEXT NOT NULL DEFAULT ''",
    )
}

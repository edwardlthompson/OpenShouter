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
        SoundLeakEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun history(): HistoryDao
    abstract fun regex(): RegexDao
    abstract fun places(): PlaceDao
    abstract fun appSpeak(): AppSpeakDao
    abstract fun reminders(): ReminderDao
    abstract fun soundLeaks(): SoundLeakDao
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

val MIGRATION_5_6 = Migration(5, 6) { db: SupportSQLiteDatabase ->
    db.execSQL(
        "ALTER TABLE notification_history ADD COLUMN kind TEXT NOT NULL DEFAULT 'NOTIFICATION'",
    )
}

val MIGRATION_6_7 = Migration(6, 7) { db: SupportSQLiteDatabase ->
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS sound_leaks (" +
            "packageName TEXT NOT NULL, " +
            "channelId TEXT NOT NULL, " +
            "channelName TEXT NOT NULL, " +
            "evidence TEXT NOT NULL, " +
            "lastSeen INTEGER NOT NULL, " +
            "PRIMARY KEY(packageName, channelId))",
    )
}

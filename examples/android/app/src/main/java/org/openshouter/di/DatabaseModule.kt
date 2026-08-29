package org.openshouter.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.openshouter.data.AppDatabase
import org.openshouter.data.AppSpeakDao
import org.openshouter.data.HistoryDao
import org.openshouter.data.MIGRATION_1_2
import org.openshouter.data.MIGRATION_2_3
import org.openshouter.data.MIGRATION_3_4
import org.openshouter.data.MIGRATION_4_5
import org.openshouter.data.MIGRATION_5_6
import org.openshouter.data.MIGRATION_6_7
import org.openshouter.data.PlaceDao
import org.openshouter.data.RegexDao
import org.openshouter.data.ReminderDao
import org.openshouter.data.SoundLeakDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "openshouter.db")
            .addMigrations(
                MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
            )
            .build()

    @Provides
    fun history(db: AppDatabase): HistoryDao = db.history()

    @Provides
    fun regex(db: AppDatabase): RegexDao = db.regex()

    @Provides
    fun places(db: AppDatabase): PlaceDao = db.places()

    @Provides
    fun appSpeak(db: AppDatabase): AppSpeakDao = db.appSpeak()

    @Provides
    fun reminders(db: AppDatabase): ReminderDao = db.reminders()

    @Provides
    fun soundLeaks(db: AppDatabase): SoundLeakDao = db.soundLeaks()
}

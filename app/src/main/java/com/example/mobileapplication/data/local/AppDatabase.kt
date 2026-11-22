package com.example.mobileapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The app's single on-device Room database, stored in [DATABASE_NAME].
 *
 * It is deliberately named for the whole app rather than one feature: other
 * features (for example saved parking permits or settings) should add their own
 * entity and DAO here instead of creating a second database. Adding a table
 * means raising [version] and adding a migration.
 *
 * Everything stored here is local demonstration data; nothing is sent to
 * Bristol City Council.
 */
@Database(
    entities = [RepairRequestEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    /** Access to the housing repair requests table. */
    abstract fun repairRequestDao(): RepairRequestDao

    /** Holds the single database instance shared by the whole app. */
    companion object {

        /** The database file name on the device. */
        const val DATABASE_NAME = "bristol_companion.db"

        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Returns the shared database, creating it the first time.
         *
         * Opening a Room database is expensive, so the app keeps exactly one.
         * The application context is used so no screen (Activity) is leaked.
         */
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
    }
}

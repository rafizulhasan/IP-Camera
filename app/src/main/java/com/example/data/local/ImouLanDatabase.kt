package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ImouCameraEntity::class,
        PtzPresetEntity::class,
        VaultEventEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ImouLanDatabase : RoomDatabase() {
    abstract fun cameraDao(): ImouCameraDao

    companion object {
        @Volatile
        private var INSTANCE: ImouLanDatabase? = null

        fun getInstance(context: Context): ImouLanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ImouLanDatabase::class.java,
                    "imou_smartlife_lan_v2.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

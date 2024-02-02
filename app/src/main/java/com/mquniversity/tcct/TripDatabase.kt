package com.mquniversity.tcct

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// https://developer.android.com/codelabs/android-room-with-a-view-kotlin
@Database(version = 1, entities = [Trip::class])
@TypeConverters(Converters::class)
abstract class TripDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: TripDatabase? = null

        fun getDatabase(context: Context): TripDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, TripDatabase::class.java, "trips.db").build()
                INSTANCE = instance
                instance
            }
        }
    }
}

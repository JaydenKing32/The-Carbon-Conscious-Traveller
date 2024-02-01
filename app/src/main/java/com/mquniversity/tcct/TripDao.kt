package com.mquniversity.tcct

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TripDao {
    @Query("SELECT * FROM trip")
    fun getAll(): List<Trip>

    @Insert
    fun insert(trip: Trip)

    @Delete
    fun delete(trip: Trip)
}

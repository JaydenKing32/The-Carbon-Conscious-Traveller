package com.mquniversity.tcct

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TripDao {
    @Query("SELECT * FROM trip")
    fun getAll(): List<Trip>

    @Query("SELECT EXISTS(SELECT * FROM trip WHERE id = :id)")
    fun doesIdExist(id: Int): Boolean

    @Insert
    fun insert(trip: Trip)

    @Delete
    fun delete(trip: Trip)

    @Query("DELETE FROM trip WHERE id = :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM trip WHERE id = (SELECT MAX(id) FROM trip)")
    fun deleteLastRow()
}

package com.mquniversity.tcct

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trip ORDER BY date DESC")
    fun getAll(): Flow<List<Trip>>

    @Query("SELECT EXISTS(SELECT * FROM trip WHERE id = :id)")
    suspend fun doesIdExist(id: Int): Boolean

    @Insert
    suspend fun insert(trip: Trip): Long

    @Delete
    suspend fun delete(trip: Trip)

    @Query("DELETE FROM trip WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM trip WHERE id = (SELECT MAX(id) FROM trip)")
    suspend fun deleteLast()
}

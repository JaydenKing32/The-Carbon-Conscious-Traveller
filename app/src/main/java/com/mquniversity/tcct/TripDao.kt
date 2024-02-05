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

    @Query("SELECT * FROM trip WHERE 0 + strftime('%d', date / 1000, 'unixepoch', 'localtime') = :day AND " +
            "0 + strftime('%m', date / 1000, 'unixepoch', 'localtime') = :month AND " +
            "0 + strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year")
    fun tripsFromDay(year: Int, month: Int, day: Int): Flow<List<Trip>>

    @Query("SELECT * FROM trip WHERE 0 + strftime('%W', date / 1000, 'unixepoch', 'localtime') = :week AND " +
            "0 + strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year")
    fun tripsFromWeek(year: Int, week: Int): Flow<List<Trip>>

    @Query("SELECT * FROM trip WHERE 0 + strftime('%m', date / 1000, 'unixepoch', 'localtime') = :month AND " +
            "0 + strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year")
    fun tripsFromMonth(year: Int, month: Int): Flow<List<Trip>>

    @Query("SELECT * FROM trip WHERE 0 + strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year")
    fun tripsFromYear(year: Int): Flow<List<Trip>>
}

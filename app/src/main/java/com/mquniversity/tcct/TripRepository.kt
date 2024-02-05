package com.mquniversity.tcct

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date

class TripRepository(private val tripDao: TripDao) {
    val allTrips: Flow<List<Trip>> = tripDao.getAll()

    @WorkerThread
    suspend fun insert(trip: Trip) {
        tripDao.insert(trip)
    }

    @WorkerThread
    suspend fun insert(trip: Trip, listener: InsertListener) {
        listener.onInsert(tripDao.insert(trip))
    }

    @WorkerThread
    suspend fun delete(trip: Trip) {
        tripDao.delete(trip)
    }

    @WorkerThread
    suspend fun delete(id: Long) {
        tripDao.delete(id)
    }

    @WorkerThread
    suspend fun deleteLast() {
        tripDao.deleteLast()
    }

    fun tripsFromDay(date: Date): Flow<List<Trip>> {
        val cal = Calendar.getInstance()
        cal.time = date
        return tripDao.tripsFromDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    fun tripsFromWeek(date: Date): Flow<List<Trip>> {
        val cal = Calendar.getInstance()
        cal.time = date
        return tripDao.tripsFromWeek(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR))
    }

    fun tripsFromMonth(date: Date): Flow<List<Trip>> {
        val cal = Calendar.getInstance()
        cal.time = date
        return tripDao.tripsFromMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun tripsFromYear(date: Date): Flow<List<Trip>> {
        val cal = Calendar.getInstance()
        cal.time = date
        return tripDao.tripsFromYear(cal.get(Calendar.YEAR))
    }
}

// https://stackoverflow.com/a/63044090
interface InsertListener {
    fun onInsert(id: Long)
}

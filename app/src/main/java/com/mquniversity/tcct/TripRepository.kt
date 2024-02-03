package com.mquniversity.tcct

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

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
}

// https://stackoverflow.com/a/63044090
interface InsertListener {
    fun onInsert(id: Long)
}

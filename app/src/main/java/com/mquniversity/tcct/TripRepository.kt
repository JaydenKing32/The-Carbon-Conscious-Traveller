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
    suspend fun deleteLast() {
        tripDao.deleteLast()
    }
}

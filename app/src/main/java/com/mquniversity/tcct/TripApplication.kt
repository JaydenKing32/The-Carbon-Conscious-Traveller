package com.mquniversity.tcct

import android.app.Application

class TripApplication : Application() {
    private val database by lazy { TripDatabase.getDatabase(this) }
    val repository by lazy { TripRepository(database.tripDao()) }
}

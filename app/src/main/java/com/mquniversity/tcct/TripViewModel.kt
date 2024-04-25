package com.mquniversity.tcct

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mquniversity.tcct.shared.Trip
import com.mquniversity.tcct.shared.TripSdk
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class TripViewModel(private val sdk: TripSdk) : ViewModel() {
    val allTrips: LiveData<List<Trip>> = sdk.getAll().asLiveData()

    fun insert(trip: Trip) = viewModelScope.launch { sdk.insert(trip) }
    fun insert(trip: Trip, listener: (Long) -> Unit) = viewModelScope.launch { listener(sdk.insert(trip)) }
    fun delete(trip: Trip) = viewModelScope.launch { sdk.delete(trip) }
    fun delete(id: Long) = viewModelScope.launch { sdk.delete(id) }
    fun setComplete(trip: Trip) = viewModelScope.launch { sdk.setComplete(trip) }

    fun tripsFromDay(instant: Instant): LiveData<List<Trip>> = sdk.tripsFromDay(instant).asLiveData()
    fun tripsFromWeek(instant: Instant): LiveData<List<Trip>> = sdk.tripsFromWeek(instant).asLiveData()
    fun tripsFromMonth(instant: Instant): LiveData<List<Trip>> = sdk.tripsFromMonth(instant).asLiveData()
    fun tripsFromYear(instant: Instant): LiveData<List<Trip>> = sdk.tripsFromYear(instant).asLiveData()
}

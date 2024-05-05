package com.mquniversity.tcct.shared

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class TripViewModel(private val sdk: TripSdk) : ViewModel() {
    private val _state = mutableStateOf(TripState())
    val state: State<TripState> = _state

    init {
        loadTrips()
    }

    fun loadTrips() {
        viewModelScope.launch {
            _state.value = _state.value.copy(trips = emptyList())
            try {
                val trips = sdk.getAll()
                _state.value = _state.value.copy(trips = trips)
            } catch (e: Exception) {
                _state.value = _state.value.copy(trips = emptyList())
            }
        }
    }

    fun insert(trip: Trip) = viewModelScope.launch { sdk.insert(trip) }
    fun insert(trip: Trip, listener: (Long) -> Unit) = viewModelScope.launch { listener(sdk.insert(trip)) }
    fun delete(trip: Trip) = viewModelScope.launch { sdk.delete(trip) }
    fun delete(id: Long) = viewModelScope.launch { sdk.delete(id) }
    fun setComplete(trip: Trip) = viewModelScope.launch { sdk.setComplete(trip) }

    fun tripsFromDay(instant: Instant): List<Trip> = sdk.tripsFromDay(instant)
    fun tripsFromWeek(instant: Instant): List<Trip> = sdk.tripsFromWeek(instant)
    fun tripsFromMonth(instant: Instant): List<Trip> = sdk.tripsFromMonth(instant)
    fun tripsFromYear(instant: Instant): List<Trip> = sdk.tripsFromYear(instant)
}

data class TripState(val trips: List<Trip> = emptyList())

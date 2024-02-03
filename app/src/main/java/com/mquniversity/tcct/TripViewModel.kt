package com.mquniversity.tcct

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TripViewModel(private val repository: TripRepository) : ViewModel() {
    val allTrips: LiveData<List<Trip>> = repository.allTrips.asLiveData()

    fun insert(trip: Trip) = viewModelScope.launch { repository.insert(trip) }
    fun insert(trip: Trip, listener: InsertListener) = viewModelScope.launch { repository.insert(trip, listener) }
    fun delete(trip: Trip) = viewModelScope.launch { repository.delete(trip) }
    fun delete(id: Long) = viewModelScope.launch { repository.delete(id) }
    fun deleteLast() = viewModelScope.launch { repository.deleteLast() }
}

class TripViewModelFactory(private val repository: TripRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

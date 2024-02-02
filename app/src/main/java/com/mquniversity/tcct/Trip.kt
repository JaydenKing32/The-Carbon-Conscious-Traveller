package com.mquniversity.tcct

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// https://developer.android.com/training/data-storage/room
@Entity
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val date: Date,
    val origin: String,
    val destination: String,
    val distance: Long,
    val vehicle: String,
    val fuel: String,
    val emissions: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Trip) return false

        if (date != other.date) return false
        if (origin != other.origin) return false
        if (destination != other.destination) return false
        if (distance != other.distance) return false
        if (vehicle != other.vehicle) return false
        if (fuel != other.fuel) return false
        return emissions == other.emissions
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + date.hashCode()
        return result
    }
}

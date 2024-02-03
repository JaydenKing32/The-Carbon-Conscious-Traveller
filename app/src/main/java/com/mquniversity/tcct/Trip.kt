package com.mquniversity.tcct

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    override fun toString(): String {
        return arrayOf(
            id,
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.forLanguageTag("en_AU")).format(date),
            "'${origin}' -> '${destination}'",
            "${distance}m",
            vehicle,
            fuel,
            CalculationUtils.formatEmission(emissions)
        ).joinToString(", ")
    }

    fun multilineString(): String {
        return buildString {
            appendLine("id = $id")
            appendLine("date = ${SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.forLanguageTag("en_AU")).format(date)}")
            appendLine("origin = $origin")
            appendLine("destination = $destination")
            appendLine("distance = ${distance}m")
            appendLine("vehicle type = $vehicle")
            appendLine("fuel type = $fuel")
            appendLine("emissions = ${CalculationUtils.formatEmission(emissions)}")
        }
    }
}

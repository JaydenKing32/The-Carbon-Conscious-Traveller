package com.mquniversity.tcct

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// https://developer.android.com/training/data-storage/room
@Entity
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val date: Date,
    val origin: String,
    @ColumnInfo(defaultValue = "0")
    val origLat: Double,
    @ColumnInfo(defaultValue = "0")
    val origLng: Double,
    val destination: String,
    @ColumnInfo(defaultValue = "0")
    val destLat: Double,
    @ColumnInfo(defaultValue = "0")
    val destLng: Double,
    val distance: Long,
    val mode: TransportMode,
    val vehicle: String,
    val fuel: String,
    val emissions: Float,
    val reduction: Float,
    @ColumnInfo(defaultValue = "0")
    var complete: Boolean = false
) {
    fun dayOfYear() = Calendar.Builder().setInstant(date).build().get(Calendar.DAY_OF_YEAR)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Trip) return false

        if (id != other.id) return false
        if (date != other.date) return false
        if (origin != other.origin) return false
        if (origLat != other.origLat) return false
        if (origLng != other.origLng) return false
        if (destination != other.destination) return false
        if (destLat != other.destLat) return false
        if (destLng != other.destLng) return false
        if (distance != other.distance) return false
        if (mode != other.mode) return false
        if (vehicle != other.vehicle) return false
        if (fuel != other.fuel) return false
        if (emissions != other.emissions) return false
        if (reduction != other.reduction) return false
        return complete == other.complete
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
            mode,
            vehicle,
            fuel,
            CalculationUtils.formatEmission(emissions),
            CalculationUtils.formatEmission(reduction),
            complete
        ).joinToString(", ")
    }

    fun multilineString(): String {
        return buildString {
            appendLine("id = $id")
            appendLine("date = ${SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.forLanguageTag("en_AU")).format(date)}")
            appendLine("origin = $origin")
            appendLine("destination = $destination")
            appendLine("mode = $mode")
            appendLine("distance = ${distance}m")
            appendLine("vehicle type = $vehicle")
            appendLine("fuel type = $fuel")
            appendLine("emissions = ${CalculationUtils.formatEmission(emissions)}")
            appendLine("emission reduction = ${CalculationUtils.formatEmission(reduction)}")
            appendLine("complete = $complete")
        }
    }
}

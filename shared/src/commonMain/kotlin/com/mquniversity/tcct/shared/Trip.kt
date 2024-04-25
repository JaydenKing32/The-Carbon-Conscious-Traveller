package com.mquniversity.tcct.shared

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

// https://cashapp.github.io/sqldelight/2.0.0/multiplatform_sqlite/
data class Trip(
    val id: Long,
    val date: Instant,
    val origin: String,
    val origLat: Double,
    val origLng: Double,
    val destination: String,
    val destLat: Double,
    val destLng: Double,
    val distance: Long,
    val mode: TransportMode,
    val vehicle: String,
    val fuel: String,
    val emissions: Float,
    val reduction: Float,
    var complete: Boolean = false
) {
    var dayOfYear = date.toLocalDateTime(TimeZone.currentSystemDefault()).dayOfYear


    override fun toString(): String {
        return arrayOf(
            id,
            dateString(),
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
            appendLine("date = ${dateString()}")
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

    fun dateString(): String =
        date.toLocalDateTime(TimeZone.currentSystemDefault()).format(LocalDateTime.Format {
            year()
            char('/')
            monthNumber()
            char('/')
            dayOfMonth()
            char(' ')
            hour()
            char(':')
            minute()
            char(':')
            second()
        })
}

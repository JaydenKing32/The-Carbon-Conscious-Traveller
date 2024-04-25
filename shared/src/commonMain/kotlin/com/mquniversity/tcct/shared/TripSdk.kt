package com.mquniversity.tcct.shared

import com.mquniversity.tcct.shared.cache.Database
import com.mquniversity.tcct.shared.cache.DatabaseDriverFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

class TripSdk(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = Database(databaseDriverFactory)

    fun getAll(): Flow<List<Trip>> = database.getAll()
    fun insert(trip: Trip): Long = database.insert(trip)
    fun deleteAll() = database.deleteAll()
    fun delete(trip: Trip) = database.delete(trip)
    fun delete(trip: Long) = database.delete(trip)
    fun setComplete(trip: Trip) = database.setComplete(trip)

    fun tripsFromDay(instant: Instant): Flow<List<Trip>> {
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return database.tripsFromDay(date.year, date.monthNumber, date.dayOfMonth)
    }

    fun tripsFromWeek(instant: Instant): Flow<List<Trip>> {
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return database.tripsFromWeek(date.year, date.getWeekNumber())
    }

    fun tripsFromMonth(instant: Instant): Flow<List<Trip>> {
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return database.tripsFromMonth(date.year, date.monthNumber)
    }

    fun tripsFromYear(instant: Instant): Flow<List<Trip>> {
        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return database.tripsFromYear(date.year)
    }

    // https://stackoverflow.com/a/76049047
    private fun LocalDateTime.getWeekNumber(): Int {
        val firstDayOfYear = LocalDateTime(year, 1, 1, 1, 1)
        val daysFromFirstDay = dayOfYear - firstDayOfYear.dayOfYear
        val firstDayOfYearDayOfWeek = firstDayOfYear.dayOfWeek.isoDayNumber
        val adjustment = when {
            firstDayOfYearDayOfWeek <= 4 -> firstDayOfYearDayOfWeek - 1
            else -> 8 - firstDayOfYearDayOfWeek
        }
        return (daysFromFirstDay + adjustment) / 7 + 1
    }
}

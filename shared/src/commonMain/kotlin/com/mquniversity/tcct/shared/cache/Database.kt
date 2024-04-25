package com.mquniversity.tcct.shared.cache

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mquniversity.tcct.shared.TransportMode
import com.mquniversity.tcct.shared.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

internal class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver(),
        com.mquniversity.tcct.shared.cache.Trip.Adapter(
            object : ColumnAdapter<Instant, Long> {
                override fun decode(databaseValue: Long): Instant = Instant.fromEpochSeconds(databaseValue)
                override fun encode(value: Instant): Long = value.epochSeconds
            },
            EnumColumnAdapter()
        ))
    private val dbQuery = database.appDatabaseQueries

    internal fun getAll(): Flow<List<Trip>> = dbQuery.getAll(::mapTrip).asFlow().mapToList(Dispatchers.IO)
    internal fun insert(trip: Trip): Long = dbQuery.insert(
        trip.date,
        trip.origin,
        trip.origLat,
        trip.origLng,
        trip.destination,
        trip.destLat,
        trip.destLng,
        trip.distance,
        trip.mode,
        trip.vehicle,
        trip.fuel,
        trip.emissions.toDouble(),
        trip.reduction.toDouble(),
        trip.complete
    ).executeAsOne()

    internal fun deleteAll() = dbQuery.deleteAll()
    internal fun delete(trip: Trip) = dbQuery.delete(trip.id)
    internal fun delete(trip: Long) = dbQuery.delete(trip)
    internal fun setComplete(trip: Trip) = dbQuery.setComplete(trip.id)

    fun tripsFromDay(year: Int, month: Int, day: Int): Flow<List<Trip>> =
        dbQuery.tripsFromDay(year.toString(), month.toString().padStart(2, '0'), day.toString().padStart(2, '0')).asFlow().map { query ->
            query.executeAsList().map {
                mapTrip(
                    it.id, it.date, it.origin, it.origLat, it.origLng, it.destination, it.destLat, it.destLng,
                    it.distance, it.mode, it.vehicle, it.fuel, it.emissions, it.reduction, it.complete
                )
            }
        }

    fun tripsFromWeek(year: Int, week: Int): Flow<List<Trip>> =
        dbQuery.tripsFromWeek(year.toString(), week.toString()).asFlow().map { query ->
            query.executeAsList().map {
                mapTrip(
                    it.id, it.date, it.origin, it.origLat, it.origLng, it.destination, it.destLat, it.destLng,
                    it.distance, it.mode, it.vehicle, it.fuel, it.emissions, it.reduction, it.complete
                )
            }
        }

    fun tripsFromMonth(year: Int, month: Int): Flow<List<Trip>> =
        dbQuery.tripsFromMonth(year.toString(), month.toString().padStart(2, '0')).asFlow().map { query ->
            query.executeAsList().map {
                mapTrip(
                    it.id, it.date, it.origin, it.origLat, it.origLng, it.destination, it.destLat, it.destLng,
                    it.distance, it.mode, it.vehicle, it.fuel, it.emissions, it.reduction, it.complete
                )
            }
        }

    fun tripsFromYear(year: Int): Flow<List<Trip>> =
        dbQuery.tripsFromYear(year.toString()).asFlow().map { query ->
            query.executeAsList().map {
                mapTrip(
                    it.id, it.date, it.origin, it.origLat, it.origLng, it.destination, it.destLat, it.destLng,
                    it.distance, it.mode, it.vehicle, it.fuel, it.emissions, it.reduction, it.complete
                )
            }
        }

    private fun mapTrip(
        id: Long,
        date: Instant,
        origin: String,
        origLat: Double,
        origLng: Double,
        destination: String,
        destLat: Double,
        destLng: Double,
        distance: Long,
        mode: TransportMode,
        vehicle: String,
        fuel: String,
        emissions: Double,
        reduction: Double,
        complete: Boolean
    ): Trip {
        return Trip(
            id,
            date,
            origin,
            origLat,
            origLng,
            destination,
            destLat,
            destLng,
            distance,
            mode,
            vehicle,
            fuel,
            emissions.toFloat(),
            reduction.toFloat(),
            complete
        )
    }
}

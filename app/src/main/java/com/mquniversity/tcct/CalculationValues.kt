package com.mquniversity.tcct

import android.content.Context
import com.google.maps.model.VehicleType
import com.opencsv.CSVReader

const val CAR = "car"
const val MOTORCYCLE = "motorcycle"
const val BUS = "bus"
const val RAIL = "rail"
const val FERRY = "ferry"

class CalculationValues(context: Context) {
    val carSizes = mutableListOf<String>()
    val carFuelTypes: Array<String>
    val carValuesMatrix = mutableListOf<FloatArray>()

    val motorcycleSizes: MutableList<String> = mutableListOf()
    val motorcycleValueMap: HashMap<String, Float> = HashMap()

    val busTypes: MutableList<String> = mutableListOf()
    val busValueMap: HashMap<String, Float> = HashMap()

    val railTypes: MutableList<String> = mutableListOf()
    val railValueMap: HashMap<String, Float> = HashMap()

    val ferryTypes: MutableList<String> = mutableListOf()
    val ferryValueMap: HashMap<String, Float> = HashMap()

    // CO2e kg/km per passenger
    val cableCarValue: Float = 0.0269f
    val trolleybusValue: Float = 0.00699f

    init {
        var nextLine: Array<String>?
        // read car calculation values
        val carReader = CSVReader(context.assets.open("${CAR}.csv").reader())
        nextLine = carReader.readNext()
        carFuelTypes = nextLine.sliceArray(1..<nextLine.size)
        while (true) {
            nextLine = carReader.readNext()
            if (nextLine == null) {
                break
            }
            if (nextLine.isEmpty()) {
                continue
            }
            carSizes.add(nextLine[0])
            val strValues = nextLine.sliceArray(1..<nextLine.size)
            val floatValues = strValues.map { if (it.isEmpty()) 0f else it.toFloat() }.toFloatArray()
            carValuesMatrix.add(floatValues)
        }

        val motorcycleReader = CSVReader(context.assets.open("${MOTORCYCLE}.csv").reader())
        motorcycleReader.skip(1)
        while (true) {
            nextLine = motorcycleReader.readNext()
            if (nextLine == null) {
                break
            }
            if (nextLine.isEmpty()) {
                continue
            }
            motorcycleSizes.add(nextLine[0])
            motorcycleValueMap[nextLine[0]] = nextLine[1].toFloat()
        }

        val busReader = CSVReader(context.assets.open("${BUS}.csv").reader())
        busReader.skip(1)
        while (true) {
            nextLine = busReader.readNext()
            if (nextLine == null) {
                break
            }
            if (nextLine.isEmpty()) {
                continue
            }
            busTypes.add(nextLine[0])
            busValueMap[nextLine[0]] = nextLine[1].toFloat()
        }

        val railReader = CSVReader(context.assets.open("${RAIL}.csv").reader())
        railReader.skip(1)
        while (true) {
            nextLine = railReader.readNext()
            if (nextLine == null) {
                break
            }
            if (nextLine.isEmpty()) {
                continue
            }
            railTypes.add(nextLine[0])
            railValueMap[nextLine[0]] = nextLine[1].toFloat()
        }

        val ferryReader = CSVReader(context.assets.open("${FERRY}.csv").reader())
        ferryReader.skip(1)
        while (true) {
            nextLine = ferryReader.readNext()
            if (nextLine == null) {
                break
            }
            if (nextLine.isEmpty()) {
                continue
            }
            ferryTypes.add(nextLine[0])
            ferryValueMap[nextLine[0]] = nextLine[1].toFloat()
        }
    }

    fun getPublicFactor(vehicleType: VehicleType): Float {
        return when (vehicleType) {
            VehicleType.BUS -> busValueMap["Average local bus"]!!
            VehicleType.INTERCITY_BUS -> busValueMap["Coach"]!!
            VehicleType.HEAVY_RAIL,
            VehicleType.HIGH_SPEED_TRAIN,
            VehicleType.LONG_DISTANCE_TRAIN -> railValueMap["National rail"]!!

            VehicleType.COMMUTER_TRAIN,
            VehicleType.METRO_RAIL,
            VehicleType.MONORAIL,
            VehicleType.RAIL,
            VehicleType.TRAM -> railValueMap["Light rail and tram"]!!

            VehicleType.SUBWAY -> railValueMap["London Underground"]!!
            VehicleType.FERRY -> ferryValueMap["Foot passenger"]!!
            VehicleType.TROLLEYBUS -> trolleybusValue
            VehicleType.CABLE_CAR -> cableCarValue
            else -> 0f
        }
    }
}

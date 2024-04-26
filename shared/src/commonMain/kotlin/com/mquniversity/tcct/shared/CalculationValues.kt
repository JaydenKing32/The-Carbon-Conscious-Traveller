package com.mquniversity.tcct.shared

val carSizes: Array<String> = arrayOf(
    "Small car",
    "Medium car",
    "Large car",
    "Mini",
    "Supermini",
    "Lower medium",
    "Upper medium",
    "Executive",
    "Luxury",
    "Sports",
    "Dual purpose 4X4",
    "MPV"
)
val carFuelTypes: Array<String> = arrayOf(
    "Diesel",
    "Petrol",
    "Hybrid",
    "CNG",
    "LPG",
    "Plug-in Hybrid Electric Vehicle",
    "Battery Electric Vehicle"
)
val carValuesMatrix: Array<Array<Float>> = arrayOf(
    arrayOf(
        0.139306448880537,
        0.140798534228188,
        0.101498857718121,
        0.0,
        0.0,
        0.0540229932885906,
        0.0482282859060403
    ),
    arrayOf(
        0.167156448880537,
        0.178188534228188,
        0.109038436241611,
        0.156604197315436,
        0.176070597315436,
        0.0850073342281879,
        0.0526663489932886
    ),
    arrayOf(
        0.208586448880537,
        0.272238534228188,
        0.1524358,
        0.238454197315436,
        0.269140597315436,
        0.101584382550336,
        0.05737
    ),
    arrayOf(
        0.107746448880537,
        0.130298534228188,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0443381932885906
    ),
    arrayOf(
        0.132146448880537,
        0.141688534228188,
        0.0,
        0.0,
        0.0,
        0.0540229932885906,
        0.0490671785234899
    ),
    arrayOf(
        0.143456448880537,
        0.164728534228188,
        0.0,
        0.0,
        0.0,
        0.0831183489932886,
        0.0525663489932886
    ),
    arrayOf(
        0.160496448880537,
        0.192108534228188,
        0.0,
        0.0,
        0.0,
        0.0868662268456376,
        0.0547703154362416
    ),
    arrayOf(
        0.173096448880537,
        0.212318534228188,
        0.0,
        0.0,
        0.0,
        0.0888617973154363,
        0.0500662563758389
    ),
    arrayOf(
        0.211196448880537,
        0.318088534228188,
        0.0,
        0.0,
        0.0,
        0.115139275167785,
        0.0583732120805369
    ),
    arrayOf(
        0.169436448880537,
        0.237158534228188,
        0.0,
        0.0,
        0.0,
        0.0996696416107383,
        0.0834804456375839
    ),
    arrayOf(
        0.201946448880537,
        0.204048534228188,
        0.0,
        0.0,
        0.0,
        0.103275582550336,
        0.0610424751677852
    ),
    arrayOf(
        0.176596448880537,
        0.184258534228188,
        0.0,
        0.0,
        0.0,
        0.0990220751677852,
        0.0793324751677852
    )
).map { it.map { inner -> inner.toFloat() }.toTypedArray() }.toTypedArray()

val motorcycleSizes: Array<String> = arrayOf("Small", "Medium", "Large")
val motorcycleValueMap: Map<String, Float> = mapOf(
    "Small" to 0.0831851865771812f,
    "Medium" to 0.10107835704698f,
    "Large" to 0.13251915704698f
)

val busTypes: Array<String> = arrayOf("Average local bus", "Coach", "Trolleybus")
val busValueMap: Map<String, Float> = mapOf(
    "Average local bus" to 0.102150394630872f,
    "Coach" to 0.0271814013422819f,
    "Trolleybus" to 0.00699f
)

val railTypes: Array<String> = arrayOf("National rail", "Light rail and tram", "London Underground")
val railValueMap: Map<String, Float> = mapOf(
    "National rail" to 0.0354629637583893f,
    "Light rail and tram" to 0.028603267114094f,
    "London Underground" to 0.027802067114094f
)

val ferryTypes: Array<String> = arrayOf("Foot passenger", "Car passenger", "Average (all passenger)")
val ferryValueMap: Map<String, Float> = mapOf(
    "Foot passenger" to 0.0187108139597315f,
    "Car passenger" to 0.129328875436242f,
    "Average (all passenger)" to 0.112698080805369f
)

// CO2e kg/km per passenger
const val cableCarValue: Float = 0.0269f
const val trolleybusValue: Float = 0.00699f

fun getPublicFactor(vehicleTypeString: String): Float {
    try {
        return when (VehicleType.valueOf(vehicleTypeString)) {
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
        }
    } catch (e: IllegalArgumentException) {
        return 0f
    }
}

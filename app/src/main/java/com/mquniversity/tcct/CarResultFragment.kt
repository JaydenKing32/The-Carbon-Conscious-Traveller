package com.mquniversity.tcct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.maps.model.TravelMode

class CarResultFragment(
    private val carSize: String,
    private val carFuelType: String
) : PrivateVehicleResultFragment() {
    override val tripMap: HashMap<Int, Long> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        travelMode = TravelMode.DRIVING
        iconResId = R.drawable.outline_directions_car_24
        factor = calculationValues
            .carValuesMatrix[calculationValues.carSizes.indexOf(carSize)][calculationValues.carFuelTypes.indexOf(carFuelType)]
        update(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return rootScrollView
    }

    fun updateFactor(carSize: String, carFuelType: String) {
        factor = calculationValues
            .carValuesMatrix[calculationValues.carSizes.indexOf(carSize)][calculationValues.carFuelTypes.indexOf(carFuelType)]
        val emissions = FloatArray(currRoutes.size)
        for (i in currRoutes.indices) {
            emissions[i] = currRoutes[i].legs[0].distance.inMeters * factor
            emissionTexts[i].text = CalculationUtils.formatEmission(emissions[i])
            distTexts[i].text = currRoutes[i].legs[0].distance.humanReadable
            durationTexts[i].text = currRoutes[i].legs[0].duration.humanReadable
        }
        mainActivity.transportSelection.updateIcons(emissions)
        updateTreeIcons()
    }

    override fun getTransportMode(): TransportMode {
        return TransportMode.CAR
    }

    override fun getVehicleType(): String {
        return carSize
    }

    override fun getFuelType(): String {
        return carFuelType
    }
}

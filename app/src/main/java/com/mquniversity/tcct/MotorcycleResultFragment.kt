package com.mquniversity.tcct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.maps.model.TravelMode

class MotorcycleResultFragment(private val motorcycleSize: String) : PrivateVehicleResultFragment() {
    override val tripMap: HashMap<Int, Long> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        travelMode = TravelMode.DRIVING
        iconResId = R.drawable.outline_sports_motorsports_24
        factor = calculationValues.motorcycleValueMap[motorcycleSize]!!
        update(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return rootScrollView
    }

    fun updateFactor(motorcycleSize: String) {
        factor = calculationValues.motorcycleValueMap[motorcycleSize]!!
        val emissions = FloatArray(currRoutes.size)
        for (i in currRoutes.indices) {
            emissions[i] = currRoutes[i].legs[0].distance.inMeters * factor
            emissionTexts[i].text = CalculationUtils.formatEmission(emissions[i])
            distTexts[i].text = currRoutes[i].legs[0].distance.humanReadable
            durationTexts[i].text = currRoutes[i].legs[0].duration.humanReadable
        }
        mainActivity.transportSelection.updateIcons(emissions)
    }

    override fun getTransportMode(): TransportMode {
        return TransportMode.MOTORCYCLE
    }

    override fun getVehicleType(): String {
        return motorcycleSize
    }

    override fun getFuelType(): String {
        return getString(R.string.no_value)
    }
}

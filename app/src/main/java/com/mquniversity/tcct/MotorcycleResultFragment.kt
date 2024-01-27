package com.mquniversity.tcct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import com.google.maps.model.TravelMode

class MotorcycleResultFragment(private val motorcycleSize: String) : PrivateVehicleResultFragment() {
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

    override fun getSpecifiedFactor(): Float {
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val specifiedBikeCalc = pref.getBoolean(getString(R.string.specified_motorcycle_calculation_key), false)

        return if (specifiedBikeCalc) {
            val bikeSize = pref.getString(getString(R.string.specified_motorcycle_size_key), getString(R.string.specified_motorcycle_size_default))
            calculationValues.motorcycleValueMap[bikeSize]!!
        } else {
            factor
        }
    }
}

package com.mquniversity.tcct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import com.google.maps.model.TravelMode

class CarResultFragment(
    private val carSize: String,
    private val carFuelType: String
) : PrivateVehicleResultFragment() {
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
        updateTreeIcons(emissions)
    }

    override fun getSpecifiedFactor(): Float {
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val specifiedCarCalc = pref.getBoolean(getString(R.string.specified_car_calculation_key), false)

        return if (specifiedCarCalc) {
            val carSize = pref.getString(getString(R.string.specified_car_size_key), getString(R.string.specified_car_size_default))
            val carFuel = pref.getString(getString(R.string.specified_car_fuel_key), getString(R.string.specified_car_fuel_default))
            calculationValues
                .carValuesMatrix[calculationValues.carSizes.indexOf(carSize)][calculationValues.carFuelTypes.indexOf(carFuel)]
        } else {
            factor
        }
    }
}

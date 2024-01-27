package com.mquniversity.tcct

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager

abstract class PrivateVehicleResultFragment : ResultFragment() {
    protected var factor = 0f
    protected var iconResId: Int? = null

    protected val emissionTexts: MutableList<TextView> = mutableListOf()
    protected val distTexts: MutableList<TextView> = mutableListOf()
    protected val durationTexts: MutableList<TextView> = mutableListOf()

    override fun update(reload: Boolean) {
        if (!reload && areLocationsSameAsBefore()) {
            showPolylines()
            mainActivity.enableButtons(true)
            return
        }
        emissionTexts.clear()
        distTexts.clear()
        durationTexts.clear()
        super.update(reload)
    }

    override fun insertRouteResult(idx: Int): Float {
        resultLayouts[idx] = layoutInflater.inflate(
            R.layout.private_vehicle_result_item,
            mainLayout,
            false) as LinearLayout

        super.insertRouteResult(idx)

        val resultLayout = resultLayouts[idx]!!
        val route = currRoutes[idx]

        resultLayout.findViewById<ImageView>(R.id.private_vehicle_icon)
            .apply {
                setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        iconResId!!,
                        context?.theme
                    )
                )
            }

        val summaryText: TextView = resultLayout.findViewById(R.id.summary_text)
        summaryText.text = getString(R.string.summary_text, route.summary)

        val emissionText: TextView = resultLayout.findViewById(R.id.emission_text)
        emissionText.text = CalculationUtils.formatEmission(route.legs[0].distance.inMeters * factor)
        val distText: TextView = resultLayout.findViewById(R.id.distance_text)
        distText.text = route.legs[0].distance.humanReadable
        val durationText: TextView = resultLayout.findViewById(R.id.duration_text)
        durationText.text = route.legs[0].duration.humanReadable

        emissionTexts.add(emissionText)
        distTexts.add(distText)
        durationTexts.add(durationText)
        return route.legs[0].distance.inMeters * factor
    }

    fun getSpecifiedFactor(): Float {
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val specifiedCarCalc = pref.getBoolean(getString(R.string.specified_car_calculation_key), false)
        val specifiedBikeCalc = pref.getBoolean(getString(R.string.specified_motorcycle_calculation_key), false)
        val useBikeInsteadOfCar = pref.getBoolean(getString(R.string.use_specified_motorcycle_instead_of_car_key), false)

        return if (specifiedCarCalc && !useBikeInsteadOfCar) {
            val carSize = pref.getString(getString(R.string.specified_car_size_key), getString(R.string.specified_car_size_default))
            val carFuel = pref.getString(getString(R.string.specified_car_fuel_key), getString(R.string.specified_car_fuel_default))
            calculationValues.carValuesMatrix[calculationValues.carSizes.indexOf(carSize)][calculationValues.carFuelTypes.indexOf(carFuel)]
        } else if (specifiedBikeCalc && useBikeInsteadOfCar) {
            val bikeSize = pref.getString(
                getString(R.string.specified_motorcycle_size_key), getString(R.string.specified_motorcycle_size_default)
            )
            calculationValues.motorcycleValueMap[bikeSize]!!
        } else {
            factor
        }
    }
}

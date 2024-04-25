package com.mquniversity.tcct

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.mquniversity.tcct.shared.CalculationUtils.formatEmission

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

    override fun getRouteEmissions(): FloatArray {
        return currRoutes.map { it.legs[0].distance.inMeters * factor }.toFloatArray()
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

        val leg = route.legs[0]
        val emission = leg.distance.inMeters * factor

        val emissionText: TextView = resultLayout.findViewById(R.id.emission_text)
        emissionText.text = formatEmission(emission)
        val distText: TextView = resultLayout.findViewById(R.id.distance_text)
        distText.text = leg.distance.humanReadable
        val durationText: TextView = resultLayout.findViewById(R.id.duration_text)
        durationText.text = leg.duration.humanReadable

        emissionTexts.add(emissionText)
        distTexts.add(distText)
        durationTexts.add(durationText)

        val button = resultLayout.findViewById<LinearLayout>(R.id.private_add_remove_button)
        button.setOnClickListener { addOrRemoveTrip(it, idx, leg, emission) }

        return emission
    }

    override fun getSpecifiedFactor(): Float {
        val result = super.getSpecifiedFactor()

        return if (result == 0f) {
            factor
        } else {
            result
        }
    }
}

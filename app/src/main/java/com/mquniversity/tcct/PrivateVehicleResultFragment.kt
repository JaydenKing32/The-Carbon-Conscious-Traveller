package com.mquniversity.tcct

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

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

        val leg = route.legs[0]
        val emission = leg.distance.inMeters * factor

        val emissionText: TextView = resultLayout.findViewById(R.id.emission_text)
        emissionText.text = CalculationUtils.formatEmission(emission)
        val distText: TextView = resultLayout.findViewById(R.id.distance_text)
        distText.text = leg.distance.humanReadable
        val durationText: TextView = resultLayout.findViewById(R.id.duration_text)
        durationText.text = leg.duration.humanReadable

        emissionTexts.add(emissionText)
        distTexts.add(distText)
        durationTexts.add(durationText)

        val button = resultLayout.findViewById<LinearLayout>(R.id.add_remove_button)
        var checked = false

        button.setOnClickListener {
            val image = it.findViewById<ImageView>(R.id.add_remove_button_image)
            checked = if (checked) {
                if (tripMap.containsKey(idx)) {
                    val trip = tripMap.remove(idx)!!
                    tripViewModel.delete(trip)
                }
                image.setImageResource(R.drawable.outline_add_circle_outline_24)
                false
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    if (!tripMap.containsKey(idx)) {
                        val trip = Trip(
                            0,
                            Calendar.getInstance().time,
                            leg.startAddress,
                            leg.endAddress,
                            leg.distance.inMeters,
                            getVehicleType(),
                            getFuelType(),
                            emission
                        )
                        tripMap[idx] = tripViewModel.repository.insert(trip)
                    }
                }
                image.setImageResource(R.drawable.outline_remove_circle_outline_24)
                true
            }
        }

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

    abstract fun getVehicleType(): String
    abstract fun getFuelType(): String
}

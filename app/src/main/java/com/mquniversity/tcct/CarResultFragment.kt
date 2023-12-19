package com.mquniversity.tcct

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.maps.DirectionsApi
import com.google.maps.errors.OverDailyLimitException
import com.google.maps.errors.OverQueryLimitException
import com.google.maps.errors.ZeroResultsException
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.TravelMode

class CarResultFragment(
    private val carSize: String,
    private val carFuelType: String
): ResultFragment() {
    private val emissionTexts: MutableList<TextView> = mutableListOf()
    private val distTexts: MutableList<TextView> = mutableListOf()
    private val durationTexts: MutableList<TextView> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        if (!isInitialized) {
            isInitialized = true
            factor = calculationValues.carValuesMatrix[calculationValues.carSizes.indexOf(carSize)][calculationValues.carFuelTypes.indexOf(carFuelType)]
            updateRouteResults()
        }
        return rootScrollView
    }

    override fun updateRouteResults() {
        mainLayout.removeAllViews()
        emissionTexts.clear()
        distTexts.clear()
        durationTexts.clear()

        val progressBar = ProgressBar(context)
        mainLayout.addView(progressBar)

        val errorText = MaterialTextView(requireContext())
        errorText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        val retryBtn = MaterialButton(ContextThemeWrapper(context, com.google.android.material.R.style.Widget_Material3_Button))
        retryBtn.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        retryBtn.text = "Try again"
        retryBtn.setOnClickListener {
            updateRouteResults()
        }
        try {
            val request = DirectionsApi.getDirections(geoApiContext, mainActivity.origin?.address, mainActivity.destination?.address)
                .mode(TravelMode.DRIVING)
                .alternatives(true)
            val response = request.await()

            mainLayout.removeAllViews()

            currRoutes = response.routes
            for (i in currRoutes.indices) {
                insertRouteResult(response.routes[i], i)
            }
        } catch (e: ZeroResultsException) {
            mainLayout.removeAllViews()
            errorText.text = "Routes were not found with the current travel mode.\nPlease try again with different travel mode."
            mainLayout.addView(errorText)
        } catch (e: OverDailyLimitException) {
            mainLayout.removeAllViews()
            errorText.text = "The requesting account has exceeded its daily quota.\nPlease try again tomorrow."
            mainLayout.addView(errorText)
            mainLayout.addView(retryBtn)
        } catch (e: OverQueryLimitException) {
            mainLayout.removeAllViews()
            errorText.text = "The requesting account has exceeded its short-term quota.\nPlease try again a few minutes later."
            mainLayout.addView(errorText)
            mainLayout.addView(retryBtn)
        } catch (e: Exception) {
            mainLayout.removeAllViews()
            errorText.text = "An error has occurred.\nPlease try again a few minutes later."
            mainLayout.addView(errorText)
            mainLayout.addView(retryBtn)
        }
    }

    fun updateFactor(carSize: String, carFuelType: String) {
        factor = calculationValues.carValuesMatrix[calculationValues.carSizes.indexOf(carSize)][calculationValues.carFuelTypes.indexOf(carFuelType)]
        for (i in currRoutes.indices) {
            emissionTexts[i].text = CalculationUtils.calculateByDistance(currRoutes[i].legs[0].distance.inMeters, factor)
            distTexts[i].text = currRoutes[i].legs[0].distance.humanReadable
            durationTexts[i].text = currRoutes[i].legs[0].duration.humanReadable
        }
    }

    override fun insertRouteResult(route: DirectionsRoute, i: Int) {
        val resultLayout = LinearLayout(context)
        resultLayout.orientation = LinearLayout.HORIZONTAL
        resultLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val iconLayout = LinearLayout(context)
        iconLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1f
        )
        iconLayout.setPadding(0, 16, 0, 0)
        val carIcon = ImageView(context)
        carIcon.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        carIcon.setImageDrawable(
            ResourcesCompat.getDrawable(
            resources,
            R.drawable.outline_directions_car_24,
            context?.theme
        ))
        iconLayout.addView(carIcon)

        val descLayout = LinearLayout(context)
        descLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            4f
        )
        descLayout.orientation = LinearLayout.VERTICAL
        val summaryText = MaterialTextView(requireContext())
        summaryText.text = "via ${route.summary}"
        summaryText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        descLayout.addView(summaryText)
        val tapForMoreText = MaterialTextView(requireContext())
        tapForMoreText.text = "Tap for more details"
        tapForMoreText.setTextColor(resources.getColor(com.google.android.libraries.places.R.color.quantum_googblue, context?.theme))
        tapForMoreText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        (tapForMoreText.layoutParams as LinearLayout.LayoutParams).apply {
            gravity = Gravity.BOTTOM
        }
        descLayout.addView(tapForMoreText)

        val valuesLayout = LinearLayout(context)
        valuesLayout.orientation = LinearLayout.VERTICAL
        valuesLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            2f
        )
        val emissionText = MaterialTextView(requireContext())
        emissionText.text = CalculationUtils.calculateByDistance(route.legs[0].distance.inMeters, factor)
        emissionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
        val distText = MaterialTextView(requireContext())
        distText.text = route.legs[0].distance.humanReadable
        distText.alpha = 0.5f
        val durationText = MaterialTextView(requireContext())
        durationText.text = route.legs[0].duration.humanReadable
        durationText.alpha = 0.5f
        emissionTexts.add(emissionText)
        distTexts.add(distText)
        durationTexts.add(durationText)
        valuesLayout.addView(emissionText)
        valuesLayout.addView(distText)
        valuesLayout.addView(durationText)

        resultLayout.addView(iconLayout)
        resultLayout.addView(descLayout)
        resultLayout.addView(valuesLayout)

        mainLayout.addView(resultLayout)
    }
}
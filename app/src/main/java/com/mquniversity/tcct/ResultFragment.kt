package com.mquniversity.tcct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsRoute

abstract class ResultFragment: Fragment() {
    protected var isInitialized = false

    protected lateinit var rootScrollView: ScrollView
    protected lateinit var mainLayout: LinearLayout

    protected lateinit var mainActivity: MainActivity
    protected lateinit var geoApiContext: GeoApiContext
    protected lateinit var calculationValues: CalculationValues

    protected lateinit var currRoutes: Array<DirectionsRoute>
    protected var factor = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!isInitialized) {
            rootScrollView = inflater.inflate(R.layout.result_view, container, false) as ScrollView
            mainLayout = LinearLayout(context)
            mainLayout.orientation = LinearLayout.VERTICAL
            mainLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // TODO? gray line divider
//            val shapeDivider = GradientDrawable()
//            shapeDivider.setSize(0, 32)
//            mainLayout.dividerDrawable = shapeDivider
//            mainLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE

            rootScrollView.addView(mainLayout)

            mainActivity = requireActivity() as MainActivity
            calculationValues = mainActivity.calculationValues
            geoApiContext = mainActivity.geoApiContext
        }


        return rootScrollView
    }

    protected abstract fun insertRouteResult(route: DirectionsRoute, i: Int)

    abstract fun updateRouteResults()
}
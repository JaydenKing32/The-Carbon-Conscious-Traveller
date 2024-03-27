package com.mquniversity.tcct

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.google.android.flexbox.FlexboxLayout
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.maps.DirectionsApi
import com.google.maps.errors.ApiException
import com.google.maps.errors.OverDailyLimitException
import com.google.maps.errors.OverQueryLimitException
import com.google.maps.errors.ZeroResultsException
import com.google.maps.model.DirectionsLeg
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.LatLng
import com.google.maps.model.TravelMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.Calendar
import kotlin.math.floor
import com.google.android.gms.maps.model.LatLng as gmsLatLng

abstract class ResultFragment : Fragment() {
    protected lateinit var rootScrollView: NestedScrollView
    protected lateinit var mainLayout: LinearLayout

    protected lateinit var mainActivity: MainActivity
    protected lateinit var calculationValues: CalculationValues

    private var currOrigin: Location? = null
    private var currDest: Location? = null

    protected lateinit var currRoutes: Array<DirectionsRoute>

    private var currPolylines: Array<Array<Polyline?>?> = arrayOf()
    private var lastClickedRoutePolylines: Array<Polyline?>? = null

    protected var resultLayouts: Array<LinearLayout?> = arrayOf()
    private var currSelectedResultLayout: LinearLayout? = null

    private var selectionIndicators: Array<View?> = arrayOf()
    private var currSelectedIndicator: View? = null

    protected var travelMode = TravelMode.DRIVING

    protected val tripViewModel: TripViewModel by activityViewModels {
        TripViewModelFactory((mainActivity.application as TripApplication).repository)
    }
    protected abstract val tripMap: HashMap<Int, Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootScrollView = NestedScrollView(requireContext())
        rootScrollView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        mainLayout = layoutInflater.inflate(
            R.layout.results_container,
            null,
            false
        ) as LinearLayout

        rootScrollView.addView(mainLayout)

        mainActivity = requireActivity() as MainActivity
        calculationValues = mainActivity.calculationValues
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return rootScrollView
    }

    private fun removePolylines() {
        mainLayout.post {
            for (polylines in currPolylines) {
                for (polyline in polylines!!) {
                    polyline?.remove()
                }
            }
        }
    }

    fun showPolylines() {
        mainLayout.post {
            for (polylines in currPolylines) {
                for (polyline in polylines!!) {
                    polyline?.isVisible = true
                }
            }
        }
    }

    fun hidePolylines() {
        mainLayout.post {
            for (polylines in currPolylines) {
                for (polyline in polylines!!) {
                    polyline?.isVisible = false
                }
            }
        }
    }

    fun findRouteIdxWithPolyline(selectedPolyline: Polyline): Int {
        var idx: Int? = null
        for (i in currPolylines.indices) {
            if (currPolylines[i]?.contains(selectedPolyline) == true) {
                idx = i
            }
        }
        return idx!!
    }

    protected open fun insertRouteResult(idx: Int): Float {
        // initialise polylines with unselected style
        for (i in currPolylines[idx]!!.indices) {
            currPolylines[idx]!![i] = mainActivity.insertPolyline(
                currRoutes[idx].legs[0].steps[i].polyline,
                resources.getColor(R.color.polyline_unselected, context?.theme),
                null
            )
        }

        mainLayout.addView(resultLayouts[idx])

        selectionIndicators[idx] = resultLayouts[idx]?.findViewById(R.id.selection_indicator)

        resultLayouts[idx]?.setOnClickListener {
            if (currSelectedResultLayout === resultLayouts[idx]) {
                return@setOnClickListener
            }
            currSelectedResultLayout = resultLayouts[idx]
            highlightRoute(idx)
            highlightResult(idx)
            mainActivity.bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        return 0f
    }

    fun highlightResult(idx: Int) {
        if (currSelectedIndicator != null) {
            currSelectedIndicator?.visibility = View.INVISIBLE
        }
        currSelectedIndicator = selectionIndicators[idx]
        selectionIndicators[idx]?.visibility = View.VISIBLE
    }

    protected fun areLocationsSameAsBefore(): Boolean {
        return ((currOrigin != null && currDest != null) &&
                (mainActivity.origin?.latitude == currOrigin?.latitude) &&
                (mainActivity.origin?.longitude == currOrigin?.longitude) &&
                (mainActivity.dest?.latitude == currDest?.latitude) &&
                (mainActivity.dest?.longitude == currDest?.longitude))
    }

    protected fun updateTreeIcons() {
        val routeEmissions = getRouteEmissions()
        if (routeEmissions.size <= 1) {
            return
        }

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val max = getMaxEmission()

        val emissionIconValues = arrayOf(
            pref.getString(getString(R.string.tree_co2_key), CalculationUtils.DEFAULT_TREE_CO2_GRAM.toString())!!.toFloat(),
            pref.getString(getString(R.string.branch_co2_key), CalculationUtils.DEFAULT_TREE_BRANCH_CO2_GRAM.toString())!!.toFloat(),
            pref.getString(getString(R.string.leaf_bundle_co2_key), CalculationUtils.DEFAULT_FOUR_LEAVES_CO2_GRAM.toString())!!.toFloat(),
            pref.getString(getString(R.string.leaf_co2_key), CalculationUtils.DEFAULT_ONE_LEAF_CO2_GRAM.toString())!!.toFloat()
        ).sortedArrayDescending()
        for (i in routeEmissions.indices) {
            val treeContainer = resultLayouts[i]?.findViewById<FlexboxLayout>(R.id.tree_container)!!
            treeContainer.removeAllViews()
            var dividend = max - routeEmissions[i]
            // Show nothing for no reduction in emissions
            // TODO: show a different symbol for increased emissions
            if (dividend == 0f || dividend < 0f) {
                continue
            }
            if (dividend < CalculationUtils.DEFAULT_ONE_LEAF_CO2_GRAM) {
                treeContainer.addView(
                    ImageView(context).apply {
                        setImageResource(R.drawable.leaf2)
                        layoutParams = FlexboxLayout.LayoutParams(
                            48,
                            48
                        )
                    }
                )
                continue
            }
            var count: Int
            for (j in emissionIconValues.indices) {
                count = floor(dividend / emissionIconValues[j]).toInt()
                if (count >= 1) {
                    val imageRes: Int = when (j) {
                        0 -> R.drawable.tree2
                        1 -> R.drawable.tree_branch3
                        2 -> R.drawable.four_leaves1
                        3 -> R.drawable.leaf2
                        else -> throw IllegalStateException("emissionIconValues add more checks")
                    }
                    repeat(count) {
                        treeContainer.addView(
                            ImageView(context).apply {
                                setImageResource(imageRes)
                                layoutParams = FlexboxLayout.LayoutParams(
                                    48,
                                    48
                                )
                            }
                        )
                    }
                }
                dividend %= emissionIconValues[j]
            }
        }
    }

    protected fun getMaxEmission(): Float {
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val specifiedCarCalc = pref.getBoolean(getString(R.string.specified_car_calculation_key), false)
        val specifiedBikeCalc = pref.getBoolean(getString(R.string.specified_motorcycle_calculation_key), false)

        // TODO: for PublicTransportResultFragment, will use the public transport route to calculate the CO2e emissions of car/bike,
        //  but would make more sense to get the actual private vehicle route and use that for calculation
        return if (specifiedCarCalc || specifiedBikeCalc) {
            val factor = getSpecifiedFactor()
            currRoutes.maxOf { it.legs[0].distance.inMeters * factor }
        } else {
            getRouteEmissions().max()
        }
    }

    abstract fun getRouteEmissions(): FloatArray

    open fun update(reload: Boolean) {
        mainLayout.removeAllViews()

        val progressBar = ProgressBar(context)
        mainLayout.addView(progressBar)

        val request = DirectionsApi.newRequest(mainActivity.geoApiContext)
            .origin(LatLng(mainActivity.origin?.latitude!!, mainActivity.origin?.longitude!!))
            .destination(LatLng(mainActivity.dest?.latitude!!, mainActivity.dest?.longitude!!))
            .mode(travelMode)
            .alternatives(true)
        tripMap.clear()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = request.await()
                removePolylines()
                currRoutes = response.routes
                if (this@ResultFragment is PublicTransportResultFragment) {
                    this@ResultFragment.fetchTransitIcons()
                }
                currPolylines = arrayOfNulls(currRoutes.size)
                for (i in currRoutes.indices) {
                    currPolylines[i] = arrayOfNulls(currRoutes[i].legs[0].steps.size)
                }
                currOrigin = mainActivity.origin!!
                currDest = mainActivity.dest!!
                resultLayouts = arrayOfNulls(currRoutes.size)
                selectionIndicators = arrayOfNulls(currRoutes.size)
                mainLayout.post {
                    mainLayout.removeAllViews()
                    val routeEmissions = currRoutes.indices.map { insertRouteResult(it) }.toFloatArray()
                    updateTreeIcons()
                    mainActivity.transportSelection.updateIcons(routeEmissions)
                    highlightRoute(0)
                    highlightResult(0)
                    mainLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                }
            } catch (e: Exception) {
                if (e !is ApiException && e !is UnknownHostException) {
                    throw e
                }
                val errorText = MaterialTextView(requireContext())
                errorText.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 32, 0, 32)
                    gravity = Gravity.CENTER
                }
                val retryBtn = MaterialButton(requireContext())
                retryBtn.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
                retryBtn.text = getString(R.string.try_again)
                retryBtn.setOnClickListener {
                    mainActivity.calculate(true)
                }
                mainLayout.post {
                    mainLayout.showDividers = LinearLayout.SHOW_DIVIDER_NONE
                    mainLayout.removeAllViews()
                    mainLayout.addView(errorText)
                    when (e) {
                        is ApiException -> {
                            if (e !is ZeroResultsException) {
                                mainLayout.addView(retryBtn)
                            }
                            when (e) {
                                is ZeroResultsException -> errorText.text = getString(R.string.error_zero_results)
                                is OverDailyLimitException -> errorText.text = getString(R.string.error_daily_limit_exceeded)
                                is OverQueryLimitException -> errorText.text = getString(R.string.error_query_limit_exceeded)
                                else -> errorText.text = getString(R.string.error_general)
                            }
                        }

                        is UnknownHostException -> {
                            errorText.text =
                                "Could not calculate results due to network error.\n" +
                                        "Make sure you have a stable network connection."
                            mainLayout.addView(retryBtn)
                        }
                    }
                }
            }
        }.invokeOnCompletion {
            mainActivity.enableButtons(true)
        }
    }

    fun highlightRoute(idx: Int) {
        val selectedPolylinePolylines = currPolylines[idx]!!
        if (lastClickedRoutePolylines === selectedPolylinePolylines) {
            return
        }
        if (lastClickedRoutePolylines != null) {
            // reset style to unselected
            for (polyline in lastClickedRoutePolylines!!) {
                polyline?.zIndex = 0f
                polyline?.color = resources.getColor(R.color.polyline_unselected, context?.theme)
                polyline?.pattern = null
            }
        }
        lastClickedRoutePolylines = selectedPolylinePolylines

        val steps = currRoutes[idx].legs[0].steps
        for (i in steps.indices) {
            selectedPolylinePolylines[i]?.zIndex = 1f
            when (steps[i].travelMode) {
                TravelMode.TRANSIT -> {
                    if (steps[i].transitDetails.line.color != null) {
                        selectedPolylinePolylines[i]?.color = Color.parseColor(
                            steps[i].transitDetails.line.color
                        )
                    } else {
                        selectedPolylinePolylines[i]?.color = resources.getColor(
                            R.color.polyline_private_vehicle,
                            context?.theme
                        )
                    }
                    selectedPolylinePolylines[i]?.pattern = null
                }

                TravelMode.DRIVING -> {
                    selectedPolylinePolylines[i]?.color = resources.getColor(
                        R.color.polyline_private_vehicle,
                        context?.theme
                    )
                    selectedPolylinePolylines[i]?.pattern = null
                }

                TravelMode.WALKING, TravelMode.BICYCLING -> {
                    selectedPolylinePolylines[i]?.color = resources.getColor(
                        R.color.polyline_private_vehicle,
                        context?.theme
                    )
                    selectedPolylinePolylines[i]?.pattern = listOf(Dot(), Gap(10f))
                }

                else -> {
                    selectedPolylinePolylines[i]?.color = resources.getColor(
                        R.color.polyline_unselected,
                        context?.theme
                    )
                    selectedPolylinePolylines[i]?.pattern = listOf(Dot(), Gap(10f))
                }
            }
        }
    }

    open fun getSpecifiedFactor(): Float {
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
            0f
        }
    }

    protected fun addOrRemoveTrip(button: View, index: Int, leg: DirectionsLeg, emission: Float) {
        val image = button.findViewById<ImageView>(R.id.add_remove_button_image)
        if (image.tag.equals(getString(R.string.button_tag_remove))) {
            if (tripMap.containsKey(index)) {
                val trip = tripMap.remove(index)!!
                tripViewModel.delete(trip)
            }
            image.tag = getString(R.string.button_tag_add)
            image.setImageResource(R.drawable.outline_add_circle_outline_24)
        } else {
            if (tripMap.containsKey(index)) {
                // Trip already exists
                return
            }

            val context = requireContext()
            if (
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                Snackbar.make(button.rootView, "Location needs to be enabled to retrieve current location", Snackbar.LENGTH_SHORT).show()
                return
            }

            val locationRequest = LocationRequest.create()
            locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
            val lsrBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(false)
            val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)
            val lsrTask: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(lsrBuilder.build())
            lsrTask.addOnSuccessListener lsrTask@{ lsr: LocationSettingsResponse? ->
                if (lsr?.locationSettingsStates?.isLocationUsable != true) {
                    return@lsrTask
                }
                val currLocRequest = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                    .build()
                val cts = CancellationTokenSource()
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val locTask = fusedLocationClient.getCurrentLocation(currLocRequest, cts.token)
                locTask.addOnSuccessListener locTask@{ curLoc ->
                    if (curLoc == null) {
                        return@locTask
                    }
                    val bound = LatLngBounds(
                        gmsLatLng(leg.startLocation.lat - VERIFICATION_RADIUS, leg.startLocation.lng - VERIFICATION_RADIUS),
                        gmsLatLng(leg.startLocation.lat + VERIFICATION_RADIUS, leg.startLocation.lng + VERIFICATION_RADIUS)
                    )

                    if (!bound.contains(gmsLatLng(curLoc.latitude, curLoc.longitude))) {
                        Snackbar.make(button.rootView, "Current location does not match start location", Snackbar.LENGTH_SHORT).show()
                        return@locTask
                    }

                    val trip = Trip(
                        0,
                        Calendar.getInstance().time,
                        leg.startAddress,
                        leg.startLocation.lat,
                        leg.startLocation.lng,
                        leg.endAddress,
                        leg.endLocation.lat,
                        leg.endLocation.lng,
                        leg.distance.inMeters,
                        getTransportMode(),
                        getVehicleType(index),
                        getFuelType(),
                        emission,
                        getMaxEmission() - emission
                    )
                    tripViewModel.insert(trip, object : InsertListener {
                        override fun onInsert(id: Long) {
                            tripMap[index] = id
                        }
                    })

                    image.setImageResource(R.drawable.outline_remove_circle_outline_24)
                    image.tag = getString(R.string.button_tag_remove)
                }
                locTask.addOnFailureListener {
                    Snackbar.make(
                        button.rootView, "Could not retrieve current location. Please try again later.", Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    abstract fun getTransportMode(): TransportMode
    abstract fun getVehicleType(index: Int): String
    abstract fun getFuelType(): String
}

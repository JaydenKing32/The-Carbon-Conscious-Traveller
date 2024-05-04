package com.mquniversity.tcct

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.mquniversity.tcct.shared.CalculationUtils.formatEmission
import com.mquniversity.tcct.shared.TransportMode
import com.mquniversity.tcct.shared.Trip
import com.mquniversity.tcct.ui.theme.colors
import com.mquniversity.tcct.ui.theme.typography
import org.koin.androidx.compose.koinViewModel

class TripActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TripsAppBar(::onSupportNavigateUp) }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

@Composable
fun TripsAppBar(backFunction: () -> Boolean) {
    MaterialTheme(colors, typography) {
        Scaffold(
            topBar = {
                TopAppBar({ Text(stringResource(R.string.trip_title)) },
                    // TODO: replace backFunction with navController
                    navigationIcon = { IconButton({ backFunction() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
                )
            }
        ) {
            TripList(Modifier.padding(it))
        }
    }
}


@Composable
fun TripList(modifier: Modifier) {
    val viewModel = koinViewModel<TripViewModel>()
    val state by remember { viewModel.state }

    // dateWeight + emissionWeight * 2 + buttonWeight * 3 = 1
    val dateWeight = 0.35f
    val emissionWeight = 0.175f
    val buttonWeight = 0.1f
    val context = LocalContext.current

    LazyColumn(modifier.fillMaxSize()) {
        item {
            Row(Modifier.background(Color.Gray)) {
                TableCell(stringResource(R.string.trip_header_vehicle), buttonWeight)
                TableCell(stringResource(R.string.trip_header_date), dateWeight)
                TableCell(stringResource(R.string.trip_header_emission), emissionWeight)
                TableCell(stringResource(R.string.trip_header_reduction), emissionWeight)
                TableCell(stringResource(R.string.trip_header_complete), buttonWeight)
                TableCell(stringResource(R.string.trip_header_delete), buttonWeight)
            }
        }
        items(items = state.trips, itemContent = {
            Row(Modifier.fillMaxWidth()) {
                Icon(
                    painterResource(when (it.mode) {
                        TransportMode.CAR -> R.drawable.outline_directions_car_24
                        TransportMode.MOTORCYCLE -> R.drawable.outline_sports_motorsports_24
                        TransportMode.PUBLIC_TRANSPORT -> R.drawable.outline_directions_subway_24
                        TransportMode.AIRPLANE -> R.drawable.outline_flight_24
                        // else -> R.drawable.outline_directions_walk_24
                    }),
                    it.mode.name,
                    Modifier.weight(buttonWeight)
                )
                val showDialog = remember { mutableStateOf(false) }
                Card {
                    if (showDialog.value) {
                        TripInfoDialog(it, showDialog.value) { showDialog.value = false }
                    }
                }
                Text(
                    it.dateString(),
                    Modifier.border(1.dp, Color.Black).weight(dateWeight).padding(8.dp).clickable { showDialog.value = true }
                )
                TableCell(formatEmission(it.emissions), emissionWeight)
                TableCell(formatEmission(it.reduction), emissionWeight)
                val tripComplete = remember { mutableStateOf(it.complete) }

                if (tripComplete.value) {
                    Icon(
                        painterResource(R.drawable.outline_check_circle_24),
                        stringResource(R.string.trip_complete_button_description),
                        Modifier.weight(buttonWeight)
                    )
                } else {
                    Icon(
                        painterResource(R.drawable.outline_cross_circle_24),
                        stringResource(R.string.trip_complete_button_description),
                        Modifier.weight(buttonWeight).clickable {
                            completeTrip(it, { viewModel.setComplete(it) }, { complete -> tripComplete.value = complete }, context)
                        })
                }

                Icon(
                    painterResource(R.drawable.outline_remove_circle_outline_24),
                    stringResource(R.string.trip_delete_button_description),
                    Modifier.weight(buttonWeight).clickable {
                        viewModel.delete(it)
                        viewModel.loadTrips()
                    }
                )
            }
        })
    }
}

// https://stackoverflow.com/a/68143597
@Composable
fun RowScope.TableCell(text: String, weight: Float) {
    Text(text, Modifier.border(1.dp, Color.Black).weight(weight).padding(8.dp), maxLines = 1)
}

@Composable
fun TripInfoDialog(trip: Trip, showDialog: Boolean, onDismiss: () -> Unit) {
    if (showDialog) {
        AlertDialog(onDismiss, {}, text = { Text(trip.multilineString()) })
    }
}

private fun completeTrip(trip: Trip, updateFun: (Trip) -> Unit, markComplete: (Boolean) -> Unit, context: Context) {
    if (
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(
            context,
            "Location needs to be enabled to retrieve current location",
            Toast.LENGTH_SHORT
        ).show()
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
                LatLng(trip.destLat - VERIFICATION_RADIUS, trip.destLng - VERIFICATION_RADIUS),
                LatLng(trip.destLat + VERIFICATION_RADIUS, trip.destLng + VERIFICATION_RADIUS)
            )

            if (!bound.contains(LatLng(curLoc.latitude, curLoc.longitude))) {
                Toast.makeText(
                    context,
                    "Current location does not match end location",
                    Toast.LENGTH_SHORT
                ).show()
                return@locTask
            }

            trip.complete = true
            markComplete(true)
            updateFun(trip)
        }
        locTask.addOnFailureListener {
            Toast.makeText(
                context,
                "Could not retrieve current location. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

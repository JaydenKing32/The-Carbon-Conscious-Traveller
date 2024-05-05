package com.mquniversity.tcct.shared

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationRequest
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task

const val VERIFICATION_RADIUS: Double = 0.01

@SuppressLint("MissingPermission")
fun completeTrip(trip: Trip, updateFun: (Trip) -> Unit, markComplete: (Boolean) -> Unit, context: Context) {
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

    val defaultInterval = 3600000L
    val priority = Priority.PRIORITY_HIGH_ACCURACY
    val locationRequest = LocationRequest.Builder(priority, defaultInterval).build()
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

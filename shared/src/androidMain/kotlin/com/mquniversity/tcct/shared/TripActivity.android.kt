package com.mquniversity.tcct.shared

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
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

@SuppressLint("MissingPermission")
actual fun completeTrip(trip: Trip, updateFun: (Trip) -> Unit, markComplete: (Boolean) -> Unit) {
    if (
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(
            appContext,
            "Location needs to be enabled to retrieve current location",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val defaultInterval = 3600000L
    val priority = Priority.PRIORITY_HIGH_ACCURACY
    val locationRequest = LocationRequest.Builder(priority, defaultInterval).build()
    val lsrBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(false)
    val settingsClient: SettingsClient = LocationServices.getSettingsClient(appContext)
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
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
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
                    appContext,
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
                appContext,
                "Could not retrieve current location. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

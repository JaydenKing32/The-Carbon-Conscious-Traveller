package com.mquniversity.tcct.shared

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val DEFAULT_LOC = LatLng(0.0, 0.0)

@Composable
@SuppressLint("MissingPermission")
actual fun Map(modifier: Modifier) {
    Setup()
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

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_LOC, DEFAULT_ZOOM)
    }
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val failureMessage = "Could not retrieve current location. Please try again later."

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        location?.let { curLoc ->
            CoroutineScope(Dispatchers.Main).launch {
                val cameraPosition = CameraPosition.fromLatLngZoom(LatLng(curLoc.latitude, curLoc.longitude), DEFAULT_ZOOM)
                cameraPositionState.animate(CameraUpdateFactory.newCameraPosition(cameraPosition), 1_000)
            }
        } ?: run {
            Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        }
    }.addOnFailureListener { e ->
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        e.message?.let { Log.e({}.javaClass.enclosingMethod?.name, it) }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true)
    )
}

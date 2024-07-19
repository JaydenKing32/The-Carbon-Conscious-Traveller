package com.mquniversity.tcct.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import cocoapods.GoogleMaps.GMSCameraPosition
import cocoapods.GoogleMaps.GMSCameraUpdate
import cocoapods.GoogleMaps.GMSMapView
import cocoapods.GoogleMaps.GMSMapView.Companion.mapWithFrame
import cocoapods.GoogleMaps.animateToZoom
import cocoapods.GoogleMaps.animateWithCameraUpdate
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject

val locationManager = CLLocationManager().apply {
    desiredAccuracy = kCLLocationAccuracyBest
    distanceFilter = 50.0
    requestWhenInUseAuthorization()
}
private val locationDelegate = LocationDelegate()

@Composable
actual fun Map(modifier: Modifier) {
    var curLat by remember { mutableStateOf(0.0) }
    var curLon by remember { mutableStateOf(0.0) }
    // https://developers.google.com/maps/documentation/ios-sdk/current-place-tutorial
    UIKitView({
        locationManager.location?.coordinate?.useContents {
            curLat = latitude
            curLon = longitude
        }
        locationDelegate.onLocationUpdate = {
            it?.let { latLon ->
                curLat = latLon.first
                curLon = latLon.second
            }
        }
        locationManager.delegate = locationDelegate
        locationManager.requestLocation()

        val camera = GMSCameraPosition.cameraWithLatitude(curLat, curLon, DEFAULT_ZOOM)

        val mapView = GMSMapView()
        mapWithFrame(mapView.frame, camera)
        mapView.settings.zoomGestures = true
        mapView.settings.consumesGesturesInView = true
        mapView.settings.myLocationButton = true
        mapView.myLocationEnabled = true
        mapView
    },
        modifier.fillMaxSize(),
        { view ->
            view.animateWithCameraUpdate(GMSCameraUpdate.setTarget(CLLocationCoordinate2DMake(curLat, curLon)))
            view.animateToZoom(DEFAULT_ZOOM)
        },
        onRelease = { it.removeFromSuperview() })
}

private class LocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {
    var onLocationUpdate: ((Pair<Double, Double>?) -> Unit)? = null

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        didUpdateLocations.firstOrNull()?.let {
            val location = it as CLLocation
            location.coordinate.useContents {
                onLocationUpdate?.invoke(Pair(latitude, longitude))
            }
        }
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        onLocationUpdate?.invoke(null)
    }
}

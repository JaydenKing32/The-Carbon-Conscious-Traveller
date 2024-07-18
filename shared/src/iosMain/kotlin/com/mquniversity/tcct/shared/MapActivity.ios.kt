package com.mquniversity.tcct.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import cocoapods.GoogleMaps.GMSCameraPosition
import cocoapods.GoogleMaps.GMSMapView
import cocoapods.GoogleMaps.GMSMapView.Companion.mapWithFrame
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLLocationAccuracyBest

@Composable
actual fun Map(modifier: Modifier) {
    UIKitView({
        // https://developers.google.com/maps/documentation/ios-sdk/current-place-tutorial
        val locationManager = CLLocationManager()
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.requestWhenInUseAuthorization()
        locationManager.distanceFilter = 50.0
        locationManager.startUpdatingLocation()

        val camera = GMSCameraPosition.cameraWithLatitude(0.0, 0.0, DEFAULT_ZOOM)
        val mapView = GMSMapView()
        mapWithFrame(mapView.frame, camera)
        mapView.settings.zoomGestures = true
        mapView.settings.consumesGesturesInView = true
        mapView.settings.myLocationButton = true
        mapView.myLocationEnabled = true
        mapView
    }, modifier.fillMaxSize(), onRelease = { it.removeFromSuperview() })
}

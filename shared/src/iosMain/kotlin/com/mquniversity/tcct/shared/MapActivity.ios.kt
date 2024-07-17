package com.mquniversity.tcct.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import cocoapods.GoogleMaps.GMSCameraPosition
import cocoapods.GoogleMaps.GMSMapView
import cocoapods.GoogleMaps.GMSMapView.Companion.mapWithFrame

@Composable
actual fun Map(modifier: Modifier) {
    UIKitView({
        val camera = GMSCameraPosition.cameraWithLatitude(0.0, 0.0, 15f)
        val mapView = GMSMapView()
        mapWithFrame(mapView.frame, camera)
        mapView.settings.zoomGestures = true
        mapView.settings.consumesGesturesInView = true
        mapView
    }, modifier.fillMaxSize(), onRelease = { it.removeFromSuperview() })
}

package com.mquniversity.tcct.shared

import androidx.compose.runtime.Composable
import cocoapods.GoogleMaps.GMSServices
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

val koinHelper = KoinHelper()

fun setup() {
    GMSServices.provideAPIKey(BuildKonfig.googleMapsApiKey)
}

@Composable
actual fun Setup() {
    setup()
}

@Composable
actual fun getViewModel(): TripViewModel {
    return koinHelper.tripViewModel
}

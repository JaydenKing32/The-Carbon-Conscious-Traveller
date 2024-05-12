package com.mquniversity.tcct.shared

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

val koinHelper = KoinHelper()

@Composable
actual fun Setup() {
    // TODO("Not yet implemented")
}

@Composable
actual fun getViewModel(): TripViewModel {
    return koinHelper.tripViewModel
}

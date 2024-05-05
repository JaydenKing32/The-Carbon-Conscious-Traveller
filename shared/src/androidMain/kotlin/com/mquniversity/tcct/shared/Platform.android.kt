package com.mquniversity.tcct.shared

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.koin.androidx.compose.koinViewModel

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

const val VERIFICATION_RADIUS: Double = 0.01

lateinit var appContext: Context

// TODO: Move to main app setup
@Composable
actual fun Setup() {
    appContext = LocalContext.current
}

@Composable
actual fun getViewModel(): TripViewModel {
    return koinViewModel<TripViewModel>()
}

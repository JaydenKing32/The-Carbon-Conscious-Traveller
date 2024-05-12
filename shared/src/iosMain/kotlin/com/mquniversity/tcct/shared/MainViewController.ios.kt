package com.mquniversity.tcct.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { TripsAppBar { true } }

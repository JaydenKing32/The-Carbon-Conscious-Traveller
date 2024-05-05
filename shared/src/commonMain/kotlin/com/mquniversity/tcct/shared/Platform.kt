package com.mquniversity.tcct.shared

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun Setup()

@Composable
expect fun getViewModel(): TripViewModel

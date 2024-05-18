package com.mquniversity.tcct.shared.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = colors,
        typography = typography,
        content = content
    )
}

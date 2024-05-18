package com.mquniversity.tcct.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mquniversity.tcct.shared.ui.theme.AppTheme

@Composable
expect fun Map(modifier: Modifier = Modifier)

@Composable
fun MapView() {
    AppTheme {
        Scaffold(Modifier.fillMaxSize()) { innerPadding ->
            Map(Modifier.padding(innerPadding))
        }
    }
}

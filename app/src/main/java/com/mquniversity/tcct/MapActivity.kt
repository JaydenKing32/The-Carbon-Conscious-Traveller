package com.mquniversity.tcct

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mquniversity.tcct.shared.MapView
import com.mquniversity.tcct.shared.ui.theme.AppTheme

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapView()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    AppTheme {
        MapView()
    }
}

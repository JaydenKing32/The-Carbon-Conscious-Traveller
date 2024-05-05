package com.mquniversity.tcct.shared

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mquniversity.tcct.shared.ui.theme.colors
import com.mquniversity.tcct.shared.ui.theme.typography
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import the_carbon_conscious_traveller.shared.generated.resources.Res
import the_carbon_conscious_traveller.shared.generated.resources.trip_title

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TripsAppBar(backFunction: () -> Boolean) {
    MaterialTheme(colors, typography) {
        Scaffold(
            topBar = {
                TopAppBar({ Text(stringResource(Res.string.trip_title)) },
                    // TODO: replace backFunction with navController
                    navigationIcon = { IconButton({ backFunction() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
                )
            }
        ) {
            TripList(Modifier.padding(it))
        }
    }


}

@Composable
expect fun TripList(modifier: Modifier)

// https://stackoverflow.com/a/68143597
@Composable
fun RowScope.TableCell(text: String, weight: Float) {
    Text(text, Modifier.border(1.dp, Color.Black).weight(weight).padding(8.dp), maxLines = 1)
}

@Composable
fun TripInfoDialog(trip: Trip, showDialog: Boolean, onDismiss: () -> Unit) {
    if (showDialog) {
        AlertDialog(onDismiss, {}, text = { Text(trip.multilineString()) })
    }
}

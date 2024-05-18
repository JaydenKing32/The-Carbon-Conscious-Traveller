package com.mquniversity.tcct.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mquniversity.tcct.shared.ui.theme.AppTheme
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import the_carbon_conscious_traveller.shared.generated.resources.Res
import the_carbon_conscious_traveller.shared.generated.resources.outline_check_circle_24
import the_carbon_conscious_traveller.shared.generated.resources.outline_cross_circle_24
import the_carbon_conscious_traveller.shared.generated.resources.outline_directions_car_24
import the_carbon_conscious_traveller.shared.generated.resources.outline_directions_subway_24
import the_carbon_conscious_traveller.shared.generated.resources.outline_flight_24
import the_carbon_conscious_traveller.shared.generated.resources.outline_remove_circle_outline_24
import the_carbon_conscious_traveller.shared.generated.resources.outline_sports_motorsports_24
import the_carbon_conscious_traveller.shared.generated.resources.trip_complete_button_description
import the_carbon_conscious_traveller.shared.generated.resources.trip_delete_button_description
import the_carbon_conscious_traveller.shared.generated.resources.trip_header_complete
import the_carbon_conscious_traveller.shared.generated.resources.trip_header_date
import the_carbon_conscious_traveller.shared.generated.resources.trip_header_delete
import the_carbon_conscious_traveller.shared.generated.resources.trip_header_emission
import the_carbon_conscious_traveller.shared.generated.resources.trip_header_reduction
import the_carbon_conscious_traveller.shared.generated.resources.trip_header_vehicle
import the_carbon_conscious_traveller.shared.generated.resources.trip_title

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TripsAppBar(backFunction: () -> Boolean) {
    Setup()
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar({ Text(stringResource(Res.string.trip_title)) },
                    // TODO: replace backFunction with navController
                    navigationIcon = { IconButton({ backFunction() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
                )
            }
        ) {
            TripList(Modifier.padding(it), getViewModel())
        }
    }
}

expect fun completeTrip(trip: Trip, updateFun: (Trip) -> Unit, markComplete: (Boolean) -> Unit)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TripList(modifier: Modifier, viewModel: TripViewModel) {
    val state by remember { viewModel.state }

    // dateWeight + emissionWeight * 2 + buttonWeight * 3 = 1
    val dateWeight = 0.35f
    val emissionWeight = 0.175f
    val buttonWeight = 0.1f

    LazyColumn(modifier.fillMaxSize()) {
        item {
            Row(Modifier.background(Color.Gray)) {
                TableCell(stringResource(Res.string.trip_header_vehicle), buttonWeight)
                TableCell(stringResource(Res.string.trip_header_date), dateWeight)
                TableCell(stringResource(Res.string.trip_header_emission), emissionWeight)
                TableCell(stringResource(Res.string.trip_header_reduction), emissionWeight)
                TableCell(stringResource(Res.string.trip_header_complete), buttonWeight)
                TableCell(stringResource(Res.string.trip_header_delete), buttonWeight)
            }
        }
        items(items = state.trips, itemContent = {
            Row(Modifier.fillMaxWidth()) {
                Icon(
                    painterResource(when (it.mode) {
                        TransportMode.CAR -> Res.drawable.outline_directions_car_24
                        TransportMode.MOTORCYCLE -> Res.drawable.outline_sports_motorsports_24
                        TransportMode.PUBLIC_TRANSPORT -> Res.drawable.outline_directions_subway_24
                        TransportMode.AIRPLANE -> Res.drawable.outline_flight_24
                        // else -> Res.drawable.outline_directions_walk_24
                    }),
                    it.mode.name,
                    Modifier.weight(buttonWeight)
                )
                val showDialog = remember { mutableStateOf(false) }
                Card {
                    if (showDialog.value) {
                        TripInfoDialog(it, showDialog.value) { showDialog.value = false }
                    }
                }
                Text(
                    it.dateString(),
                    Modifier.border(1.dp, Color.Black).weight(dateWeight).padding(8.dp).clickable { showDialog.value = true }
                )
                TableCell(CalculationUtils.formatEmission(it.emissions), emissionWeight)
                TableCell(CalculationUtils.formatEmission(it.reduction), emissionWeight)
                val tripComplete = remember { mutableStateOf(it.complete) }

                if (tripComplete.value) {
                    Icon(
                        painterResource(Res.drawable.outline_check_circle_24),
                        stringResource(Res.string.trip_complete_button_description),
                        Modifier.weight(buttonWeight)
                    )
                } else {
                    Icon(
                        painterResource(Res.drawable.outline_cross_circle_24),
                        stringResource(Res.string.trip_complete_button_description),
                        Modifier.weight(buttonWeight).clickable {
                            completeTrip(it, { viewModel.setComplete(it) }, { complete -> tripComplete.value = complete })
                        })
                }

                Icon(
                    painterResource(Res.drawable.outline_remove_circle_outline_24),
                    stringResource(Res.string.trip_delete_button_description),
                    Modifier.weight(buttonWeight).clickable {
                        viewModel.delete(it)
                        viewModel.loadTrips()
                    }
                )
            }
        })
    }
}

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

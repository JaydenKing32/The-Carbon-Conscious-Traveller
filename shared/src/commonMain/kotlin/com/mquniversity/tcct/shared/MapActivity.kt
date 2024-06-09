@file:OptIn(ExperimentalResourceApi::class)

package com.mquniversity.tcct.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mquniversity.tcct.shared.ui.theme.AppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import the_carbon_conscious_traveller.shared.generated.resources.Res
import the_carbon_conscious_traveller.shared.generated.resources.about_title
import the_carbon_conscious_traveller.shared.generated.resources.github_link
import the_carbon_conscious_traveller.shared.generated.resources.outline_history_24
import the_carbon_conscious_traveller.shared.generated.resources.outline_info_24
import the_carbon_conscious_traveller.shared.generated.resources.outline_privacy_tip_24
import the_carbon_conscious_traveller.shared.generated.resources.outline_settings_24
import the_carbon_conscious_traveller.shared.generated.resources.outline_show_chart_24
import the_carbon_conscious_traveller.shared.generated.resources.privacy_link
import the_carbon_conscious_traveller.shared.generated.resources.privacy_policy_title
import the_carbon_conscious_traveller.shared.generated.resources.settings_title
import the_carbon_conscious_traveller.shared.generated.resources.stats_title
import the_carbon_conscious_traveller.shared.generated.resources.title_activity_maps
import the_carbon_conscious_traveller.shared.generated.resources.trip_title

// https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation-routing.html
// https://developer.android.com/guide/navigation
enum class Screen(val title: StringResource) {
    Map(title = Res.string.title_activity_maps),
    Settings(title = Res.string.settings_title),
    Trips(title = Res.string.trip_title),
    Stats(title = Res.string.stats_title)
}

@Composable
expect fun Map(modifier: Modifier = Modifier)

@Composable
fun MapView(navController: NavHostController = rememberNavController(), modifier: Modifier = Modifier) {
    AppTheme {
        val state = rememberScaffoldState()
        val scope = rememberCoroutineScope()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentScreen = Screen.valueOf(backStackEntry?.destination?.route ?: Screen.Map.name)

        Scaffold(modifier.statusBarsPadding(), state, drawerGesturesEnabled = state.drawerState.isOpen,
            drawerContent = {
                MapDrawer(modifier) {
                    // Close menu when switching screens
                    scope.launch { state.drawerState.close() }
                    navController.navigate(Screen.Trips.name)
                }
            },
            topBar = {
                MapAppBar(
                    currentScreen,
                    navController.previousBackStackEntry != null,
                    { navController.navigateUp() },
                    { scope.launch { if (state.drawerState.isClosed) state.drawerState.open() else state.drawerState.open() } },
                    modifier
                )
            }
        ) { innerPadding ->
            NavHost(navController, Screen.Map.name, modifier.padding(innerPadding)) {
                composable(Screen.Map.name) { Map(modifier) }
                composable(Screen.Trips.name) { TripList(modifier, getViewModel()) }
            }
        }
    }
}

@Composable
fun MapAppBar(currentScreen: Screen, canNavigateBack: Boolean, navigateUp: () -> Unit, openMenu: () -> Unit, modifier: Modifier) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if (currentScreen == Screen.Map) {
                IconButton(openMenu) { Icon(Icons.Default.Menu, "Menu") }
            } else if (canNavigateBack) {
                IconButton(navigateUp) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            }
        }
    )
}

@Composable
fun DrawerItem(modifier: Modifier, icon: DrawableResource, text: StringResource, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().clickable { onClick() }) {
        Icon(vectorResource(icon), null)
        Text(stringResource(text), style = MaterialTheme.typography.h5)
    }
    Spacer(Modifier.height(24.dp))
}

@Composable
fun MapDrawer(modifier: Modifier = Modifier, tripsNav: () -> Unit) {
    Column(modifier.fillMaxSize().padding(start = 24.dp, top = 48.dp)) {
        val uriHandler = LocalUriHandler.current
        val scope = rememberCoroutineScope()

        DrawerItem(modifier, Res.drawable.outline_info_24, Res.string.about_title) {
            scope.launch { uriHandler.openUri(getString(Res.string.github_link)) }
        }
        DrawerItem(modifier, Res.drawable.outline_privacy_tip_24, Res.string.privacy_policy_title) {
            scope.launch { uriHandler.openUri(getString(Res.string.privacy_link)) }
        }
        DrawerItem(modifier, Res.drawable.outline_settings_24, Res.string.settings_title) {}
        DrawerItem(modifier, Res.drawable.outline_history_24, Res.string.trip_title, tripsNav)
        DrawerItem(modifier, Res.drawable.outline_show_chart_24, Res.string.stats_title) {}
    }
}

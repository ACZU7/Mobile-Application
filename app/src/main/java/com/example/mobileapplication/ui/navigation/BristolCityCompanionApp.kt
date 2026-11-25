package com.example.mobileapplication.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.dashboard.DashboardViewModel
import com.example.mobileapplication.ui.dashboard.StaffScreen
import com.example.mobileapplication.data.repository.ParkingRepository
import com.example.mobileapplication.data.repository.StreetReportRepository
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobileapplication.ui.cleanairzone.CLEAN_AIR_ZONE_GRAPH_ROUTE
import com.example.mobileapplication.ui.cleanairzone.cleanAirZoneGraph
import com.example.mobileapplication.ui.housing.HOUSING_GRAPH_ROUTE
import com.example.mobileapplication.ui.housing.housingGraph
import com.example.mobileapplication.ui.parking.parkingGraph
import com.example.mobileapplication.ui.streetissues.STREET_CAMERA_ROUTE
import com.example.mobileapplication.ui.streetissues.streetIssuesGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BristolCityCompanionApp() {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val isParking = currentDestination?.hierarchy?.any { it.route == AppDestination.Parking.route } == true
    val isStreets = currentDestination?.hierarchy?.any { it.route == AppDestination.Streets.route } == true
    val isCleanAirZone = currentDestination?.hierarchy?.any { it.route == CLEAN_AIR_ZONE_GRAPH_ROUTE } == true
    val isHousing = currentDestination?.hierarchy?.any { it.route == HOUSING_GRAPH_ROUTE } == true

    Scaffold(
        topBar = {
            if (!isParking && !isStreets && !isHousing && !isCleanAirZone && currentDestination?.route != AppDestination.Admin.route) {
            CenterAlignedTopAppBar(
                title = { if (currentDestination?.route == AppDestination.Profile.route)
                    Text(stringResource(R.string.profile_nav_title)) else Text("Bristol City Companion") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            }
        },
        bottomBar = {
            // Mockup 5: the street-issue camera is full screen, so the bottom bar is hidden only there.
            if (currentDestination?.route != STREET_CAMERA_ROUTE) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AppDestination.bottomNavigation.forEach { destination ->
                    NavigationBarItem(
                        // hierarchy also matches nested graphs, so Housing stays selected on every housing screen.
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true ||
                            (destination == AppDestination.Profile && currentDestination?.route == AppDestination.Admin.route),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = stringResource(destination.labelRes)) },
                        label = { Text(stringResource(destination.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Home.route,
            // consumeWindowInsets tells screens with their own Scaffold that the system bars
            // are already handled here, so they don't add the same space again.
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(AppDestination.Home.route) {
                PlaceholderScreen("Welcome to Bristol City Companion", "A prototype for selected Bristol resident services.") {
                    // TEMPORARY: opens the Clean Air Zone checker for testing until Home has service tiles.
                    TextButton(onClick = { navController.navigate(CLEAN_AIR_ZONE_GRAPH_ROUTE) }) {
                        Text(stringResource(R.string.temp_open_clean_air_zone))
                    }
                }
            }
            composable(AppDestination.Profile.route) {
                // Integration placeholder only: Mohammad owns the full Profile dashboard.
                // TODO(Mohammad): retain Omar's Admin entry here when implementing Profile.
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    TextButton(onClick = { navController.navigate(AppDestination.Admin.route) }) {
                        Text(stringResource(R.string.profile_admin_entry))
                    }
                }
            }
            composable(AppDestination.Admin.route) {
                val context = LocalContext.current.applicationContext
                val vm: DashboardViewModel = viewModel(factory = viewModelFactory {
                    initializer { DashboardViewModel(StreetReportRepository.getInstance(context), ParkingRepository.getInstance(context)) }
                })
                StaffScreen(vm) { navController.popBackStack() }
            }
            parkingGraph(navController)
            streetIssuesGraph(navController)
            housingGraph(navController)
            cleanAirZoneGraph(navController)
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, description: String, extra: @Composable () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            text = description,
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        extra()
    }
}

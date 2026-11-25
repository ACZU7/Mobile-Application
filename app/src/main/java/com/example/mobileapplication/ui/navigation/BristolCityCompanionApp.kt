package com.example.mobileapplication.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.dashboard.*
import com.example.mobileapplication.data.repository.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mobileapplication.ui.cleanairzone.cleanAirZoneGraph
import com.example.mobileapplication.ui.housing.housingGraph
import com.example.mobileapplication.ui.parking.parkingGraph
import com.example.mobileapplication.ui.streetissues.STREET_CAMERA_ROUTE
import com.example.mobileapplication.ui.streetissues.streetIssuesGraph

@Composable
fun BristolCityCompanionApp() {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    Scaffold(
        bottomBar = {
            // Mockup 5: the street-issue camera is full screen, so the bottom bar is hidden only there.
            if (currentDestination?.route != STREET_CAMERA_ROUTE) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AppDestination.bottomNavigation.forEach { destination ->
                    NavigationBarItem(
                        // hierarchy also matches nested graphs, so Housing stays selected on every housing screen.
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true ||
                            (destination == AppDestination.Profile && currentDestination?.route == "staff"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            if (destination == AppDestination.Home) {
                                navController.popBackStack(AppDestination.Home.route, false)
                            } else {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
                val context = LocalContext.current.applicationContext
                val vm: DashboardViewModel = viewModel(factory = viewModelFactory {
                    initializer { DashboardViewModel(StreetReportRepository.getInstance(context), ParkingRepository.getInstance(context)) }
                })
                val reports by vm.reports.collectAsStateWithLifecycle()
                HomeScreen(reports) { navController.navigate(it) { launchSingleTop = true } }
            }
            composable(AppDestination.Profile.route) {
                ProfileScreen { navController.navigate("staff") }
            }
            composable("staff") {
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

package com.example.mobileapplication.ui.parking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

const val PARKING_GRAPH_ROUTE = "parking"

object ParkingRoutes {
    const val LIST = "parking/list"
    const val DETAILS = "parking/details"
}

/** Graph-scoped state keeps the selected location and filters across both screens. */
fun NavGraphBuilder.parkingGraph(navController: NavHostController) {
    navigation(startDestination = ParkingRoutes.LIST, route = PARKING_GRAPH_ROUTE) {
        composable(ParkingRoutes.LIST) { entry ->
            val graphEntry = remember(entry) { navController.getBackStackEntry(PARKING_GRAPH_ROUTE) }
            val viewModel: ParkingViewModel = viewModel(viewModelStoreOwner = graphEntry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            ParkingListScreen(
                state = state,
                onTypeSelected = viewModel::onTypeSelected,
                onAreaSelected = viewModel::onAreaSelected,
                onClearFilters = viewModel::clearFilters,
                onFinderFilterSelected = viewModel::onFinderFilterSelected,
                onMapPinSelected = viewModel::highlightCarPark,
                onFiltersVisibleChanged = viewModel::setFiltersVisible,
                onLocationMessageVisibleChanged = viewModel::setLocationMessageVisible,
                onCarParkSelected = {
                    viewModel.selectCarPark(it)
                    navController.navigate(ParkingRoutes.DETAILS) { launchSingleTop = true }
                }
            )
        }
        composable(ParkingRoutes.DETAILS) { entry ->
            val graphEntry = remember(entry) { navController.getBackStackEntry(PARKING_GRAPH_ROUTE) }
            val viewModel: ParkingViewModel = viewModel(viewModelStoreOwner = graphEntry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            ParkingDetailsScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onDirections = viewModel::showDirectionsMessage,
                onDismissDirections = viewModel::dismissDirectionsMessage
            )
        }
    }
}

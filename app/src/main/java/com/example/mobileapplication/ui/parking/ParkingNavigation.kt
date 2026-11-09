package com.example.mobileapplication.ui.parking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
            val requestLocation = rememberLocationRequest(
                onLocating = viewModel::onLocating,
                onLocation = viewModel::onLocationReceived,
                onUnavailable = viewModel::onLocationUnavailable,
                onDenied = viewModel::onLocationDenied
            )
            ParkingListScreen(
                state = state,
                onTypeSelected = viewModel::onTypeSelected,
                onAreaSelected = viewModel::onAreaSelected,
                onClearFilters = viewModel::clearFilters,
                onFinderFilterSelected = viewModel::onFinderFilterSelected,
                onMapPinSelected = viewModel::highlightCarPark,
                onFiltersVisibleChanged = viewModel::setFiltersVisible,
                onFindLocation = requestLocation,
                onDismissLocationMessage = viewModel::dismissLocationMessage,
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
            val context = LocalContext.current
            val directionsLauncher = remember(context) { MapsDirectionsLauncher(context) }
            val carParkName = state.selectedCarPark?.let { stringResource(it.nameRes) }.orEmpty()
            ParkingDetailsScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onDirections = viewModel::requestDirections,
                onConfirmDirections = {
                    val carPark = state.selectedCarPark
                    viewModel.onDirectionsLaunched(
                        carPark != null && directionsLauncher.open(carPark.latitude, carPark.longitude, carParkName)
                    )
                },
                onDismissDirections = viewModel::dismissDirections,
                onDismissDirectionsUnavailable = viewModel::dismissDirectionsUnavailable
            )
        }
    }
}

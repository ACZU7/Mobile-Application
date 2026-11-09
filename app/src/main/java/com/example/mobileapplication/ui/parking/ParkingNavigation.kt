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
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobileapplication.data.repository.DataStorePermitStore
import com.example.mobileapplication.ui.parking.permit.PermitViewModel
import com.example.mobileapplication.ui.parking.permit.PermitZoneScreen
import com.example.mobileapplication.ui.parking.permit.WorkManagerPermitReminderScheduler
import com.example.mobileapplication.ui.parking.permit.openNotificationSettings
import com.example.mobileapplication.ui.parking.permit.rememberNotificationPermissionRequest

const val PARKING_GRAPH_ROUTE = "parking"

object ParkingRoutes {
    const val LIST = "parking/list"
    const val DETAILS = "parking/details"
    const val PERMIT = "parking/permit"
}

/** Graph-scoped state keeps the selected location and filters across the finder and details screens. */
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
                onOpenPermitZones = { navController.navigate(ParkingRoutes.PERMIT) { launchSingleTop = true } },
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
        composable(ParkingRoutes.PERMIT) {
            val context = LocalContext.current
            val viewModel: PermitViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        PermitViewModel(
                            DataStorePermitStore(context.applicationContext),
                            WorkManagerPermitReminderScheduler(context.applicationContext)
                        )
                    }
                }
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val enableReminder = rememberNotificationPermissionRequest(
                onAllowed = viewModel::enableReminder,
                onBlocked = viewModel::onNotificationsBlocked
            )
            PermitZoneScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onReminderChanged = { on -> if (on) enableReminder() else viewModel.disableReminder() },
                onZoneSelected = viewModel::showZoneInfo,
                onPermitSelected = viewModel::showPermitDetails,
                onDismissDialog = viewModel::dismissDialog,
                onOpenNotificationSettings = { openNotificationSettings(context) }
            )
        }
    }
}

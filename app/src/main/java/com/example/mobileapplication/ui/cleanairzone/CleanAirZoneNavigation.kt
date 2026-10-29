package com.example.mobileapplication.ui.cleanairzone

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

/** Route of the whole Clean Air Zone section. The main navigation navigates here to open it. */
const val CLEAN_AIR_ZONE_GRAPH_ROUTE = "cleanairzone"

/** The routes of the individual screens inside the Clean Air Zone section. */
object CleanAirZoneRoutes {
    /** The registration checker (the first screen). */
    const val CHECKER = "cleanairzone/checker"
}

/**
 * Adds the Clean Air Zone screens to the app's navigation as one nested graph.
 *
 * To plug it into the main app, call `cleanAirZoneGraph(navController)` inside the
 * main `NavHost { ... }` and navigate to [CLEAN_AIR_ZONE_GRAPH_ROUTE].
 */
fun NavGraphBuilder.cleanAirZoneGraph(navController: NavHostController) {
    navigation(startDestination = CleanAirZoneRoutes.CHECKER, route = CLEAN_AIR_ZONE_GRAPH_ROUTE) {

        composable(CleanAirZoneRoutes.CHECKER) { entry ->
            val viewModel: CleanAirZoneViewModel = viewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            // Only offer a back arrow if there is a screen to go back to.
            val canLeave = remember(entry) { navController.previousBackStackEntry != null }
            CleanAirZoneScreen(
                state = state,
                demoRegistrations = viewModel.demoRegistrations,
                onRegistrationChanged = viewModel::onRegistrationChanged,
                onCheck = viewModel::onCheck,
                onBack = if (canLeave) ({ navController.popBackStack() }) else null
            )
        }
    }
}

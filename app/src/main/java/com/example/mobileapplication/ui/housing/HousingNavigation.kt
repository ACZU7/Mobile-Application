package com.example.mobileapplication.ui.housing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

/** Route of the whole Housing section. The main navigation navigates here to open it. */
const val HOUSING_GRAPH_ROUTE = "housing"

/** The routes of the individual screens inside the Housing section. */
object HousingRoutes {
    /** The repair form (the first screen). */
    const val FORM = "housing/form"

    /** The confirmation shown after a successful submit. */
    const val CONFIRMATION = "housing/confirmation"

    /** The progress screen for the selected request. */
    const val PROGRESS = "housing/progress"
}

/**
 * Adds the Housing repair screens to the app's navigation as one nested graph.
 *
 * To plug it into the main app, call `housingGraph(navController)` inside the
 * main `NavHost { ... }` and navigate to [HOUSING_GRAPH_ROUTE].
 *
 * All three screens share one [HousingViewModel] (see [housingViewModel]), so the
 * request saved on the form is still available on the confirmation and progress screens.
 */
fun NavGraphBuilder.housingGraph(navController: NavHostController) {
    navigation(startDestination = HousingRoutes.FORM, route = HOUSING_GRAPH_ROUTE) {

        composable(HousingRoutes.FORM) { entry ->
            val viewModel = housingViewModel(navController, entry)
            val state by viewModel.formState.collectAsStateWithLifecycle()
            RepairFormScreen(
                state = state,
                onRoomSelected = viewModel::onRoomSelected,
                onIssueTypeSelected = viewModel::onIssueTypeSelected,
                onDescriptionChanged = viewModel::onDescriptionChanged,
                onAppointmentSelected = viewModel::onAppointmentSelected,
                onSubmit = {
                    if (viewModel.submit()) {
                        // Remove the form from the back stack so pressing Back on the
                        // confirmation screen cannot resubmit the same request.
                        navController.navigate(HousingRoutes.CONFIRMATION) {
                            popUpTo(HousingRoutes.FORM) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(HousingRoutes.CONFIRMATION) { entry ->
            val viewModel = housingViewModel(navController, entry)
            val request by viewModel.selectedRequest.collectAsStateWithLifecycle()
            RepairConfirmationScreen(
                request = request,
                onViewProgress = { navController.navigate(HousingRoutes.PROGRESS) },
                onDone = {
                    navController.navigate(HousingRoutes.FORM) {
                        popUpTo(HousingRoutes.CONFIRMATION) { inclusive = true }
                    }
                }
            )
        }

        composable(HousingRoutes.PROGRESS) { entry ->
            val viewModel = housingViewModel(navController, entry)
            val request by viewModel.selectedRequest.collectAsStateWithLifecycle()
            RepairProgressScreen(
                request = request,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Returns the [HousingViewModel] shared by every screen in the Housing graph.
 *
 * Normally `viewModel()` gives each screen its own ViewModel. Here we tie it to
 * the back-stack entry of the whole Housing graph instead, so all three screens
 * get the same instance, and it is cleared when the user leaves the Housing section.
 */
@Composable
private fun housingViewModel(
    navController: NavHostController,
    entry: NavBackStackEntry
): HousingViewModel {
    val graphEntry = remember(entry) { navController.getBackStackEntry(HOUSING_GRAPH_ROUTE) }
    return viewModel(viewModelStoreOwner = graphEntry)
}

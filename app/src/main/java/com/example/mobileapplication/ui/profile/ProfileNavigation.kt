package com.example.mobileapplication.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation

/** Route of the Profile tab. The bottom navigation navigates here. */
const val PROFILE_GRAPH_ROUTE = "profile"

/** The routes of the screens inside the Profile tab. */
object ProfileRoutes {
    /** The profile summary (the first screen). */
    const val VIEW = "profile/view"

    /** The edit form. */
    const val EDIT = "profile/edit"
}

/**
 * Adds the Profile screens as one nested graph. Both screens share one
 * [ProfileViewModel], so the edit form starts from the values shown on the profile.
 */
fun NavGraphBuilder.profileGraph(navController: NavHostController) {
    navigation(startDestination = ProfileRoutes.VIEW, route = PROFILE_GRAPH_ROUTE) {

        composable(ProfileRoutes.VIEW) { entry ->
            val viewModel = profileViewModel(navController, entry)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            ProfileScreen(
                state = state,
                onEdit = {
                    viewModel.startEditing()
                    navController.navigate(ProfileRoutes.EDIT) { launchSingleTop = true }
                },
                onClearRequested = viewModel::onClearRequested,
                onClearDismissed = viewModel::onClearDismissed,
                onClearConfirmed = viewModel::onClearConfirmed,
                onSignOut = viewModel::signOut,
                onMessageShown = viewModel::onMessageShown
            )
        }

        composable(ProfileRoutes.EDIT) { entry ->
            val viewModel = profileViewModel(navController, entry)
            val form by viewModel.form.collectAsStateWithLifecycle()
            ProfileScreenEdit(
                form = form,
                viewModel = viewModel,
                // dropUnlessResumed ignores a second tap while the screen is already closing,
                // so a double tap on Save cannot pop the Profile screen as well.
                onClose = dropUnlessResumed { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun ProfileScreenEdit(form: ProfileFormState, viewModel: ProfileViewModel, onClose: () -> Unit) {
    EditProfileScreen(
        form = form,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onPostcodeChanged = viewModel::onPostcodeChanged,
        onVehicleChanged = viewModel::onVehicleChanged,
        onSave = { if (viewModel.save()) onClose() },
        onCancel = onClose
    )
}

/** The [ProfileViewModel] shared by both profile screens, cleared when the tab's back stack goes. */
@Composable
private fun profileViewModel(navController: NavHostController, entry: NavBackStackEntry): ProfileViewModel {
    val graphEntry = remember(entry) { navController.getBackStackEntry(PROFILE_GRAPH_ROUTE) }
    return viewModel(viewModelStoreOwner = graphEntry, factory = ProfileViewModel.Factory)
}

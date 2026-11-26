package com.example.mobileapplication.ui.auth

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/** Route of the Login screen, shown when nobody is signed in. */
const val LOGIN_ROUTE = "login"

/**
 * Adds the Login screen. It never navigates itself: the app shell moves to Home
 * when the saved session changes, so Login is never left on the back stack.
 */
fun NavGraphBuilder.loginScreen() {
    composable(LOGIN_ROUTE) {
        val viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
        val state by viewModel.state.collectAsStateWithLifecycle()
        LoginScreen(
            state = state,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
            onSignIn = viewModel::onSignIn,
            onContinueAsGuest = viewModel::onContinueAsGuest
        )
    }
}

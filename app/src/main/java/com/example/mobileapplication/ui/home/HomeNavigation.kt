package com.example.mobileapplication.ui.home

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.mobileapplication.data.model.RepairRequest

/** Route of the Home dashboard, the app's start destination. */
const val HOME_ROUTE = "home"

/**
 * Adds the Home dashboard to the app's navigation. Where each tile leads is
 * decided by the app shell, because the tiles open screens inside other tabs.
 */
fun NavGraphBuilder.homeScreen(
    onServiceSelected: (HomeService) -> Unit,
    onLatestRepairSelected: (RepairRequest) -> Unit
) {
    composable(HOME_ROUTE) {
        val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val query by viewModel.searchText.collectAsStateWithLifecycle()
        HomeScreen(
            state = state,
            query = query,
            onQueryChanged = viewModel::onQueryChanged,
            onClearQuery = viewModel::onClearQuery,
            onServiceSelected = onServiceSelected,
            onLatestRepairSelected = onLatestRepairSelected
        )
    }
}

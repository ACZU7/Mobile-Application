package com.example.mobileapplication.ui.streetissues

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.composable

const val STREET_GRAPH_ROUTE = "streets"
private const val FORM = "streets/form"
private const val CONFIRMATION = "streets/confirmation"
private const val REPORTS = "streets/reports"
private const val STATUS = "streets/status/{reference}"

fun NavGraphBuilder.streetIssuesGraph(navController: NavHostController) {
    navigation(startDestination = FORM, route = STREET_GRAPH_ROUTE) {
        composable(FORM) { entry ->
            val vm = streetViewModel(navController, entry)
            val form by vm.formState.collectAsStateWithLifecycle()
            StreetFormScreen(form, vm::category, vm::description, vm::location,
                onSubmit = { if (vm.submit()) navController.navigate(CONFIRMATION) { popUpTo(FORM) } },
                onReports = { navController.navigate(REPORTS) }, onBack = { navController.popBackStack() })
        }
        composable(CONFIRMATION) { entry ->
            val vm = streetViewModel(navController, entry)
            val report by vm.selectedReport.collectAsStateWithLifecycle()
            StreetConfirmationScreen(report,
                onStatus = { report?.let { navController.navigate("streets/status/${it.reference}") } },
                onAnother = { navController.popBackStack(FORM, false) }, onBack = { navController.popBackStack() })
        }
        composable(REPORTS) { entry ->
            val vm = streetViewModel(navController, entry)
            val reports by vm.reports.collectAsStateWithLifecycle()
            val filter by vm.filter.collectAsStateWithLifecycle()
            StreetReportsScreen(reports, filter, vm::filter,
                onReport = { navController.navigate("streets/status/$it") }, onBack = { navController.popBackStack() })
        }
        composable(STATUS, arguments = listOf(navArgument("reference") { type = NavType.StringType })) { entry ->
            val vm = streetViewModel(navController, entry)
            val reports by vm.reports.collectAsStateWithLifecycle()
            val report = reports.find { it.reference == entry.arguments?.getString("reference") }
            StreetStatusScreen(report, onReports = {
                navController.navigate(REPORTS) { popUpTo(FORM); launchSingleTop = true }
            }, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun streetViewModel(navController: NavHostController, entry: NavBackStackEntry): StreetIssuesViewModel {
    val graph = remember(entry) { navController.getBackStackEntry(STREET_GRAPH_ROUTE) }
    return viewModel(viewModelStoreOwner = graph)
}

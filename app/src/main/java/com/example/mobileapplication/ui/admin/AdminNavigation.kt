package com.example.mobileapplication.ui.admin

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.mobileapplication.R
import com.example.mobileapplication.data.repository.ParkingRepository
import com.example.mobileapplication.data.repository.StreetReportRepository
import com.example.mobileapplication.ui.dashboard.AnalyticsScreen
import com.example.mobileapplication.ui.dashboard.DashboardViewModel
import com.example.mobileapplication.ui.dashboard.ParkingAdminScreen
import com.example.mobileapplication.ui.dashboard.ReportsScreen
import com.example.mobileapplication.ui.dashboard.StaffFilters
import com.example.mobileapplication.ui.navigation.ADMIN_GRAPH_ROUTE
import com.example.mobileapplication.ui.navigation.BottomTab

/** The routes of the admin section's screens, one per bottom tab. */
object AdminRoutes {
    /** Street reports (the first screen admins see). */
    const val REPORTS = "admin/reports"

    /** Sample car park editing. */
    const val PARKING = "admin/parking"

    /** Report and repair counts. */
    const val ANALYTICS = "admin/analytics"

    /** Who is signed in, and Sign out. */
    const val ACCOUNT = "admin/account"
}

/**
 * The admin section's bottom tabs: Reports, Parking, Analytics, Account.
 * Admins see these instead of the resident tabs.
 */
enum class AdminDestination(
    override val route: String,
    @StringRes override val labelRes: Int,
    override val icon: ImageVector
) : BottomTab {
    REPORTS(AdminRoutes.REPORTS, R.string.staff_reports, Icons.Default.ReportProblem),
    PARKING(AdminRoutes.PARKING, R.string.staff_parking, Icons.Default.LocalParking),
    ANALYTICS(AdminRoutes.ANALYTICS, R.string.staff_analytics, Icons.Default.BarChart),
    ACCOUNT(AdminRoutes.ACCOUNT, R.string.admin_account_tab, Icons.Default.AccountCircle)
}

/**
 * Holds the street report filters for the whole admin section, so the Reports
 * and Analytics tabs share them, as the views of the original staff screen did.
 */
class StaffFiltersHolder : ViewModel() {
    val filters = StaffFilters()
}

/**
 * Adds the admin section: one nested graph whose screens share Omar's
 * [DashboardViewModel] and the street report filters.
 * Only admins may be here; the app shell checks that with RouteAccess.
 */
fun NavGraphBuilder.adminGraph(navController: NavHostController) {
    navigation(startDestination = AdminRoutes.REPORTS, route = ADMIN_GRAPH_ROUTE) {
        composable(AdminRoutes.REPORTS) { entry ->
            ReportsScreen(dashboardViewModel(navController, entry), filtersHolder(navController, entry).filters)
        }
        composable(AdminRoutes.PARKING) { entry ->
            ParkingAdminScreen(dashboardViewModel(navController, entry))
        }
        composable(AdminRoutes.ANALYTICS) { entry ->
            AnalyticsScreen(
                vm = dashboardViewModel(navController, entry),
                filters = filtersHolder(navController, entry).filters
            )
        }
        composable(AdminRoutes.ACCOUNT) {
            val viewModel: AdminAccountViewModel = viewModel(factory = AdminAccountViewModel.Factory)
            val session by viewModel.session.collectAsStateWithLifecycle()
            AdminAccountScreen(session = session, onSignOut = viewModel::signOut)
        }
    }
}

/** The back-stack entry of the whole admin graph, which owns the shared ViewModels. */
@Composable
private fun adminGraphEntry(navController: NavHostController, entry: NavBackStackEntry): NavBackStackEntry =
    remember(entry) { navController.getBackStackEntry(ADMIN_GRAPH_ROUTE) }

/** Omar's [DashboardViewModel], created the same way as on the original staff route, shared by the admin tabs. */
@Composable
private fun dashboardViewModel(navController: NavHostController, entry: NavBackStackEntry): DashboardViewModel {
    val context = LocalContext.current.applicationContext
    return viewModel(viewModelStoreOwner = adminGraphEntry(navController, entry), factory = viewModelFactory {
        initializer { DashboardViewModel(StreetReportRepository.getInstance(context), ParkingRepository.getInstance(context)) }
    })
}

@Composable
private fun filtersHolder(navController: NavHostController, entry: NavBackStackEntry): StaffFiltersHolder =
    viewModel(viewModelStoreOwner = adminGraphEntry(navController, entry))

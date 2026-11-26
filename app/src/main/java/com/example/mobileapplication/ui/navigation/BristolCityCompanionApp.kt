package com.example.mobileapplication.ui.navigation

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobileapplication.ui.dashboard.DashboardViewModel
import com.example.mobileapplication.ui.dashboard.StaffScreen
import com.example.mobileapplication.data.repository.ParkingRepository
import com.example.mobileapplication.data.repository.StreetReportRepository
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.auth.LOGIN_ROUTE
import com.example.mobileapplication.ui.auth.SessionState
import com.example.mobileapplication.ui.auth.SessionViewModel
import com.example.mobileapplication.ui.auth.loginScreen
import com.example.mobileapplication.ui.cleanairzone.CLEAN_AIR_ZONE_GRAPH_ROUTE
import com.example.mobileapplication.ui.cleanairzone.cleanAirZoneGraph
import com.example.mobileapplication.ui.home.HOME_ROUTE
import com.example.mobileapplication.ui.home.HomeService
import com.example.mobileapplication.ui.home.homeScreen
import com.example.mobileapplication.ui.housing.HousingRoutes
import com.example.mobileapplication.ui.housing.housingGraph
import com.example.mobileapplication.ui.parking.ParkingRoutes
import com.example.mobileapplication.ui.parking.parkingGraph
import com.example.mobileapplication.ui.profile.profileGraph
import com.example.mobileapplication.ui.streetissues.STREET_CAMERA_ROUTE
import com.example.mobileapplication.ui.streetissues.streetIssuesGraph
import com.example.mobileapplication.ui.theme.extendedColors
import kotlinx.coroutines.launch

/**
 * The app shell: the bottom navigation and the navigation host.
 *
 * There is no app-wide top bar. Every screen draws its own [com.example.mobileapplication.ui.shared.TopBar],
 * and Home has its own teal header, so the status bar area is teal on Home only.
 *
 * The app opens on Login when nobody is signed in, otherwise on Home. When the
 * saved session changes (sign in, continue as guest, sign out) the shell moves
 * between Login and Home and clears the old back stack, so Back never returns
 * to Login after signing in, or into the app after signing out.
 */
@Composable
fun BristolCityCompanionApp() {
    val sessionViewModel: SessionViewModel = viewModel(factory = SessionViewModel.Factory)
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()

    if (sessionState == SessionState.Loading) {
        // Wait for the saved session, so Login doesn't flash up for someone already signed in.
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val isHome = currentDestination?.route == HOME_ROUTE
    val isLogin = currentDestination?.route == LOGIN_ROUTE
    val startDestination = remember { if (sessionState is SessionState.SignedIn) HOME_ROUTE else LOGIN_ROUTE }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(sessionState) { navController.followSession(sessionState) }
    StatusBarIcons(onTealHeader = isHome)

    Scaffold(
        // Behind the status bar: teal on Home so its header reaches the top of the screen,
        // and the Login background on Login.
        containerColor = when {
            isHome -> MaterialTheme.colorScheme.primary
            isLogin -> MaterialTheme.extendedColors.surfaceTint
            else -> MaterialTheme.colorScheme.background
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Mockup 5: the street-issue camera is full screen, so the bottom bar is hidden there.
            // It is hidden on Login too, because nothing behind it is open until someone signs in.
            val showBar = currentDestination != null && currentDestination.route != STREET_CAMERA_ROUTE && !isLogin
            if (showBar) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AppDestination.bottomNavigation.forEach { destination ->
                    val label = stringResource(destination.labelRes)
                    NavigationBarItem(
                        // isSelected also matches nested graphs, so a tab stays selected on every screen inside it.
                        selected = destination.isSelected(currentDestination),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = { navController.switchToTab(destination) },
                        // The label below already names the tab, so the icon has no description of its own.
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            // consumeWindowInsets tells screens with their own Scaffold that the system bars
            // are already handled here, so they don't add the same space again.
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            loginScreen()
            homeScreen(
                onServiceSelected = { navController.openService(it) },
                onLatestRepairSelected = { request ->
                    navController.openInTab(
                        AppDestination.Housing,
                        tabStartRoute = HousingRoutes.HOME,
                        screenRoute = HousingRoutes.progressFor(request.reference)
                    )
                }
            )
            parkingGraph(navController)
            streetIssuesGraph(navController)
            housingGraph(navController)
            cleanAirZoneGraph(navController)
            profileGraph(navController, onAdmin = {
                navController.navigate(AppDestination.Admin.route) { launchSingleTop = true }
            })
            composable(AppDestination.Admin.route) {
                val role = (sessionState as? SessionState.SignedIn)?.session?.role
                if (RouteAccess.isAllowed(ADMIN_ROUTE, role)) {
                    val context = LocalContext.current.applicationContext
                    val vm: DashboardViewModel = viewModel(factory = viewModelFactory {
                        initializer { DashboardViewModel(StreetReportRepository.getInstance(context), ParkingRepository.getInstance(context)) }
                    })
                    StaffScreen(vm) { navController.popBackStack() }
                } else {
                    // Only admins may open Omar's staff dashboard: send anyone else back.
                    val message = stringResource(R.string.admin_only_message)
                    LaunchedEffect(Unit) {
                        // The shell's scope, because this screen's own effects stop when it is popped.
                        scope.launch { snackbarHostState.showSnackbar(message) }
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}

/**
 * Moves between Login and Home when the saved session changes.
 *
 * - Signed in on the Login screen: go to Home and remove Login, so Back from Home leaves the app.
 * - Signed out anywhere else: go to Login and remove everything, including the
 *   tabs' saved back stacks, so the next person can't get back into the previous one's screens.
 */
private fun NavHostController.followSession(state: SessionState) {
    val onLogin = currentDestination?.route == LOGIN_ROUTE
    when (state) {
        is SessionState.SignedIn -> if (onLogin) {
            navigate(HOME_ROUTE) { popUpTo(LOGIN_ROUTE) { inclusive = true } }
        }
        SessionState.SignedOut -> if (!onLogin) {
            navigate(LOGIN_ROUTE) { popUpTo(graph.id) { inclusive = true } }
            AppDestination.bottomNavigation.forEach { clearBackStack(it.route) }
        }
        SessionState.Loading -> Unit
    }
}

/**
 * Switches to [tab] like tapping it in the bottom bar: each tab keeps its own
 * back stack, which is saved when leaving and restored when coming back.
 * Home is always the bottom of the stack once someone is signed in.
 */
private fun NavHostController.switchToTab(tab: AppDestination) {
    navigate(tab.route) {
        popUpTo(HOME_ROUTE) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Opens [screenRoute] inside [tab], on top of the tab's first screen
 * ([tabStartRoute]), so Back returns to that tab's first screen and then Home.
 */
private fun NavHostController.openInTab(tab: AppDestination, tabStartRoute: String, screenRoute: String) {
    switchToTab(tab)
    navigate(screenRoute) {
        popUpTo(tabStartRoute)
        launchSingleTop = true
    }
}

/** Where each Home tile leads. */
private fun NavHostController.openService(service: HomeService) {
    when (service) {
        HomeService.FIND_PARKING -> openInTab(AppDestination.Parking, ParkingRoutes.LIST, ParkingRoutes.LIST)
        HomeService.PERMIT_ZONES -> openInTab(AppDestination.Parking, ParkingRoutes.LIST, ParkingRoutes.PERMIT)
        // Mockup 8 shows the Clean Air Zone checker inside the Parking tab.
        HomeService.CLEAN_AIR_ZONE -> openInTab(AppDestination.Parking, ParkingRoutes.LIST, CLEAN_AIR_ZONE_GRAPH_ROUTE)
        // The report form is the first screen of the Streets tab.
        HomeService.REPORT_ISSUE -> switchToTab(AppDestination.Streets)
        HomeService.REPORT_REPAIR -> openInTab(AppDestination.Housing, HousingRoutes.HOME, HousingRoutes.FORM)
        HomeService.RENT_ACCOUNT -> openInTab(AppDestination.Housing, HousingRoutes.HOME, HousingRoutes.RENT_ACCOUNT)
    }
}

/**
 * Status bar icons must contrast with what is behind them: the teal Home header,
 * or the plain surface everywhere else.
 */
@Composable
private fun StatusBarIcons(onTealHeader: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    val darkTheme = isSystemInDarkTheme()
    val headerIsLight = MaterialTheme.colorScheme.primary.luminance() > 0.5f
    LaunchedEffect(onTealHeader, darkTheme, headerIsLight) {
        val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
        // "Light status bar" means dark icons, for a light background.
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
            if (onTealHeader) headerIsLight else !darkTheme
    }
}

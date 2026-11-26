package com.example.mobileapplication.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.cleanairzone.CLEAN_AIR_ZONE_GRAPH_ROUTE
import com.example.mobileapplication.ui.home.HOME_ROUTE
import com.example.mobileapplication.ui.profile.PROFILE_GRAPH_ROUTE

/**
 * The five resident (and guest) bottom navigation tabs, in the order of the mockups.
 * Admins get the admin section's tabs instead.
 *
 * @property route The tab's route (a nested graph for every tab except Home).
 * @property labelRes The tab label.
 * @property alsoSelectedFor Other top-level graphs that are shown inside this tab.
 * The Clean Air Zone checker is opened from the Parking tab (mockup 8 shows
 * Parking selected), so Parking stays highlighted there.
 */
sealed class AppDestination(
    override val route: String,
    @StringRes override val labelRes: Int,
    override val icon: ImageVector,
    private val alsoSelectedFor: Set<String> = emptySet()
) : BottomTab {
    data object Home : AppDestination(HOME_ROUTE, R.string.nav_home, Icons.Default.Home)
    data object Parking : AppDestination(
        "parking",
        R.string.nav_parking,
        Icons.Default.DirectionsCar,
        alsoSelectedFor = setOf(CLEAN_AIR_ZONE_GRAPH_ROUTE)
    )
    data object Streets : AppDestination("streets", R.string.nav_streets, Icons.Default.ReportProblem)
    data object Housing : AppDestination("housing", R.string.nav_housing, Icons.Default.HomeRepairService)
    data object Profile : AppDestination(PROFILE_GRAPH_ROUTE, R.string.nav_profile, Icons.Default.Person)

    /** True when [destination] belongs to this tab, so its icon is highlighted. */
    override fun isSelected(destination: NavDestination?): Boolean =
        destination?.hierarchy?.any { it.route == route || it.route in alsoSelectedFor } == true

    companion object {
        val bottomNavigation get() = listOf(Home, Parking, Streets, Housing, Profile)
    }
}

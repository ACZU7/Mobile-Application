package com.example.mobileapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mobileapplication.R

sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    val labelRes: Int get() = when (this) {
        Home -> R.string.nav_home
        Parking -> R.string.nav_parking
        Streets -> R.string.nav_streets
        Housing -> R.string.nav_housing
        Admin -> R.string.admin_nav_title
    }
    data object Home : AppDestination("home", "Home", Icons.Default.Home)
    data object Parking : AppDestination("parking", "Parking", Icons.Default.DirectionsCar)
    data object Streets : AppDestination("streets", "Streets", Icons.Default.ReportProblem)
    data object Housing : AppDestination("housing", "Housing", Icons.Default.HomeRepairService)
    data object Admin : AppDestination("staff", "Admin", Icons.Default.AdminPanelSettings)

    companion object {
        val bottomNavigation get() = listOf(Home, Parking, Streets, Housing, Admin)
    }
}

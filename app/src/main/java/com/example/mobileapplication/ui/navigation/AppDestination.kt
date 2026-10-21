package com.example.mobileapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : AppDestination("home", "Home", Icons.Default.Home)
    data object Parking : AppDestination("parking", "Parking", Icons.Default.DirectionsCar)
    data object Streets : AppDestination("streets", "Streets", Icons.Default.ReportProblem)
    data object Housing : AppDestination("housing", "Housing", Icons.Default.HomeRepairService)

    companion object {
        val bottomNavigation = listOf(Home, Parking, Streets, Housing)
    }
}

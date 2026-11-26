package com.example.mobileapplication.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy

/**
 * One item in a bottom navigation bar. Residents and guests get the
 * [AppDestination] tabs; admins get the admin section's tabs.
 */
interface BottomTab {
    /** Where the tab leads. */
    val route: String

    /** The tab label. */
    @get:StringRes
    val labelRes: Int

    /** The tab icon. */
    val icon: ImageVector

    /** True when [destination] belongs to this tab, so its icon is highlighted. */
    fun isSelected(destination: NavDestination?): Boolean =
        destination?.hierarchy?.any { it.route == route } == true
}

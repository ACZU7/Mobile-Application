package com.example.mobileapplication.ui.navigation

import com.example.mobileapplication.data.model.Role
import com.example.mobileapplication.ui.auth.LOGIN_ROUTE
import com.example.mobileapplication.ui.home.HOME_ROUTE

/** Route of the admin section (a nested graph holding the admin tabs). */
const val ADMIN_GRAPH_ROUTE = "admin"

/** Which part of the app a screen belongs to. */
enum class AppArea {
    /** The Login screen. */
    LOGIN,

    /** The resident tabs: Home, Parking, Streets, Housing, Profile and everything inside them. */
    RESIDENT,

    /** The admin section: Reports, Repairs, Parking, Analytics, Account. */
    ADMIN
}

/**
 * Who may open which part of the app. Guests and residents use the resident
 * tabs; admins use the admin section only; nobody signed in sees only Login.
 */
object RouteAccess {

    /** The area of a screen, from its route and its parent graphs' routes ([routes]). */
    fun areaOf(routes: Collection<String?>): AppArea = when {
        LOGIN_ROUTE in routes -> AppArea.LOGIN
        ADMIN_GRAPH_ROUTE in routes -> AppArea.ADMIN
        else -> AppArea.RESIDENT
    }

    /** True when someone with [role] may be in [area]. A null role means nobody is signed in. */
    fun isAllowed(area: AppArea, role: Role?): Boolean = when (area) {
        AppArea.LOGIN -> true
        AppArea.RESIDENT -> role == Role.GUEST || role == Role.RESIDENT
        AppArea.ADMIN -> role == Role.ADMIN
    }

    /** Where someone with [role] starts: Reports for admins, Home for everyone else, Login when signed out. */
    fun startRouteFor(role: Role?): String = when (role) {
        Role.ADMIN -> ADMIN_GRAPH_ROUTE
        Role.RESIDENT, Role.GUEST -> HOME_ROUTE
        null -> LOGIN_ROUTE
    }
}

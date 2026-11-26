package com.example.mobileapplication.ui.navigation

import com.example.mobileapplication.data.model.Role

/** Route of Omar's Admin (staff) dashboard. */
const val ADMIN_ROUTE = "staff"

/**
 * Who may open which route. Every signed-in user (guest, resident or admin) may
 * use the resident features; only admins may open the Admin (staff) dashboard.
 */
object RouteAccess {

    /** True when someone with [role] may open [route]. A null role means nobody is signed in. */
    fun isAllowed(route: String, role: Role?): Boolean = when (route) {
        ADMIN_ROUTE -> role == Role.ADMIN
        else -> role != null
    }

    /** True when the "Admin dashboard" button should be shown to someone with [role]. */
    fun canOpenAdmin(role: Role?): Boolean = isAllowed(ADMIN_ROUTE, role)
}

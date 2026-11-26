package com.example.mobileapplication.ui.navigation

import com.example.mobileapplication.data.model.Role
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for who may open the Admin route and the resident features. */
class RouteAccessTest {

    @Test
    fun onlyAdminsMayOpenTheAdminRoute() {
        assertTrue(RouteAccess.isAllowed(ADMIN_ROUTE, Role.ADMIN))
        assertFalse(RouteAccess.isAllowed(ADMIN_ROUTE, Role.RESIDENT))
        assertFalse(RouteAccess.isAllowed(ADMIN_ROUTE, Role.GUEST))
        assertFalse(RouteAccess.isAllowed(ADMIN_ROUTE, null))
    }

    @Test
    fun theAdminButtonIsOnlyForAdmins() {
        assertTrue(RouteAccess.canOpenAdmin(Role.ADMIN))
        assertFalse(RouteAccess.canOpenAdmin(Role.RESIDENT))
        assertFalse(RouteAccess.canOpenAdmin(Role.GUEST))
    }

    @Test
    fun everyoneSignedIn_mayUseResidentFeatures() {
        listOf("home", "parking", "streets", "housing", "profile", "cleanairzone").forEach { route ->
            Role.entries.forEach { role -> assertTrue("$route/$role", RouteAccess.isAllowed(route, role)) }
            assertFalse(route, RouteAccess.isAllowed(route, null))
        }
    }
}

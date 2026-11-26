package com.example.mobileapplication.ui.navigation

import com.example.mobileapplication.data.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for who may use the admin section, the resident tabs and Login. */
class RouteAccessTest {

    @Test
    fun areaOf_findsTheAreaFromTheRouteAndItsParents() {
        assertEquals(AppArea.LOGIN, RouteAccess.areaOf(listOf("login", null)))
        assertEquals(AppArea.ADMIN, RouteAccess.areaOf(listOf("admin/repairs", "admin", null)))
        assertEquals(AppArea.RESIDENT, RouteAccess.areaOf(listOf("home", null)))
        assertEquals(AppArea.RESIDENT, RouteAccess.areaOf(listOf("housing/my-repairs", "housing", null)))
    }

    @Test
    fun onlyAdminsMayUseTheAdminSection() {
        assertTrue(RouteAccess.isAllowed(AppArea.ADMIN, Role.ADMIN))
        assertFalse(RouteAccess.isAllowed(AppArea.ADMIN, Role.RESIDENT))
        assertFalse(RouteAccess.isAllowed(AppArea.ADMIN, Role.GUEST))
        assertFalse(RouteAccess.isAllowed(AppArea.ADMIN, null))
    }

    @Test
    fun guestsAndResidentsUseTheResidentTabs_butAdminsDoNot() {
        assertTrue(RouteAccess.isAllowed(AppArea.RESIDENT, Role.GUEST))
        assertTrue(RouteAccess.isAllowed(AppArea.RESIDENT, Role.RESIDENT))
        assertFalse(RouteAccess.isAllowed(AppArea.RESIDENT, Role.ADMIN))
        assertFalse(RouteAccess.isAllowed(AppArea.RESIDENT, null))
    }

    @Test
    fun loginIsOpenToEveryone() {
        (Role.entries + null).forEach { assertTrue(RouteAccess.isAllowed(AppArea.LOGIN, it)) }
    }

    @Test
    fun eachRoleStartsInItsOwnSection() {
        assertEquals(ADMIN_GRAPH_ROUTE, RouteAccess.startRouteFor(Role.ADMIN))
        assertEquals("home", RouteAccess.startRouteFor(Role.RESIDENT))
        assertEquals("home", RouteAccess.startRouteFor(Role.GUEST))
        assertEquals("login", RouteAccess.startRouteFor(null))
    }
}

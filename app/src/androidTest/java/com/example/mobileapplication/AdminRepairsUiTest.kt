package com.example.mobileapplication

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Role
import com.example.mobileapplication.data.repository.RepairRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

/**
 * The admin section end to end: admins see only the admin tabs, can change a
 * housing repair's status on the Repairs tab, and the tenant's "My repairs"
 * shows the new status after signing out and continuing as a guest.
 */
class AdminRepairsUiTest {
    val ui = createAndroidComposeRule<MainActivity>()

    // Signs in as the demo admin before the activity starts, so the test begins in the admin section.
    @get:Rule val signedIn: RuleChain = RuleChain.outerRule(SignedInRule(Role.ADMIN)).around(ui)

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun waitFor(text: String, substring: Boolean = false) =
        ui.waitUntil(10_000) { ui.onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty() }

    private fun tab(label: String) = ui.onNode(hasText(label) and isSelectable()).performClick()

    private fun tap(text: String) {
        val node = ui.onNodeWithText(text)
        if (!node.isDisplayed()) node.performScrollTo()
        node.performClick()
    }

    @Test
    fun adminTabsOnly_andARepairStatusChangeReachesMyRepairs() = runBlocking {
        val repairs = RepairRepository.getInstance(context)
        val request = repairs.requests.first().first()
        val original = request.status
        val desired = if (original == RepairStatus.COMPLETED) RepairStatus.SUBMITTED else RepairStatus.COMPLETED
        val desiredLabel = context.getString(desired.labelRes)
        try {
            waitFor("All statuses")
            ui.onNode(hasText("Reports") and isSelectable()).assertIsSelected()
            listOf("Reports", "Repairs", "Parking", "Analytics", "Account").forEach {
                ui.onAllNodes(hasText(it) and isSelectable()).assertCountEquals(1)
            }
            listOf("Home", "Streets", "Housing", "Profile").forEach {
                ui.onAllNodes(hasText(it) and isSelectable()).assertCountEquals(0)
            }

            tab("Repairs")
            waitFor("All rooms")
            // The newest request is the first card; its dropdown shows its current status.
            ui.onAllNodes(hasText(context.getString(original.labelRes)) and hasClickAction()).onFirst().performClick()
            ui.onNode(hasText(desiredLabel) and hasAnyAncestor(isPopup())).performClick()
            ui.onAllNodes(hasText("Update") and isEnabled()).onFirst().performClick()
            ui.waitUntil(10_000) { runBlocking { repairs.getRequest(request.reference)?.status == desired } }
            waitFor("Saved on this device.")

            tab("Analytics")
            waitFor("By room")

            tab("Account")
            waitFor("Signed in as admin@example.com (Admin)")
            tap("Sign out")
            waitFor("Continue as guest")
            tap("Continue as guest")
            ui.waitUntil(10_000) { ui.onAllNodes(hasText("Housing") and isSelectable()).fetchSemanticsNodes().isNotEmpty() }
            tab("Housing")
            tap("My repairs")
            // The row shows the reference with the room, for example "HR-2026-0003 · Kitchen".
            waitFor(request.reference, substring = true)
            // The tapped row (reference and status together) shows the status the admin chose.
            ui.onNode(hasText(request.reference, substring = true) and hasText(desiredLabel, substring = true)).assertExists()
        } finally {
            repairs.updateStatus(request.reference, original)
        }
        Unit
    }
}

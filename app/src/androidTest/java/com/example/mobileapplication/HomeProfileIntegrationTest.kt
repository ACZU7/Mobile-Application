package com.example.mobileapplication

import com.example.mobileapplication.data.model.Role
import org.junit.rules.RuleChain
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import android.graphics.Bitmap
import java.io.File

/**
 * Cross-tab navigation in the resident tabs: every Home tile, the Profile editor,
 * and no way into the admin section (admins have their own tabs).
 */
class HomeProfileIntegrationTest {
    val ui = createAndroidComposeRule<MainActivity>()
    // Signs in as the demo resident before the activity starts, so the test begins on Home rather than Login.
    @get:Rule val signedIn: RuleChain = RuleChain.outerRule(SignedInRule(Role.RESIDENT)).around(ui)
    private fun tab(label: String) = ui.onNode(hasText(label) and isSelectable()).performClick()
    private fun visible(text: String) {
        ui.waitUntil(10000) { ui.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        val node = ui.onNodeWithText(text)
        if (!node.isDisplayed()) node.performScrollTo()
        node.assertIsDisplayed()
    }
    private fun screenshot(name: String) {
        ui.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val dir = File(instrumentation.targetContext.filesDir, "pr8-evidence").apply { mkdirs() }
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    @Test fun homeServicesAndProfileEditorStayInTheResidentTabs() {
        visible("Find parking")
        screenshot("01-home")
        listOf("Find parking" to "Nearest car parks", "Permit zones" to "Your resident's permit",
            "Report an issue" to "Submit report", "Clean Air Zone" to "Clean Air Zone checker",
            "Report a repair" to "Which room is the problem in?", "Rent account" to "Pay rent").forEach { (tile, destination) ->
            ui.onNodeWithText(tile).performScrollTo().performClick()
            visible(destination)
            tab("Home")
            visible("Find parking")
        }
        tab("Profile")
        visible("Your details")
        visible("Signed in as resident@example.com (Resident)")
        // The staff features moved to the admin section: residents have no way in.
        ui.onAllNodesWithText("Admin dashboard").assertCountEquals(0)
        listOf("Reports", "Repairs", "Analytics", "Account").forEach {
            ui.onAllNodes(hasText(it) and isSelectable()).assertCountEquals(0)
        }
        screenshot("02-profile")
        val edit = ui.onNode(hasText("Set up profile") or hasText("Edit details"))
        edit.performScrollTo().performClick()
        visible("Edit profile")
        ui.onNodeWithText("Cancel").performScrollTo().performClick()
        visible("Your details")
        ui.onNode(hasText("Profile") and isSelectable()).assertIsSelected()
    }
}

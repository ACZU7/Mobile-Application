package com.example.mobileapplication

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import android.graphics.Bitmap
import java.io.File

/** Cross-tab navigation after combining Mohammad's Home/Profile with Omar's Staff route. */
class HomeProfileIntegrationTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
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
    @Test fun homeServicesAndProfileEditorKeepAdminReachable() {
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
        visible("Admin dashboard")
        screenshot("02-profile-admin")
        val edit = ui.onNode(hasText("Set up profile") or hasText("Edit details"))
        edit.performScrollTo().performClick()
        visible("Edit profile")
        ui.onNodeWithText("Cancel").performScrollTo().performClick()
        visible("Admin dashboard")
        ui.onNodeWithText("Admin dashboard").performClick()
        visible("Bristol Connect Staff")
        ui.onNode(hasText("Profile") and isSelectable()).assertIsSelected()
        screenshot("03-staff")
        Espresso.pressBack()
        visible("Your details")
        ui.onNode(hasText("Profile") and isSelectable()).assertIsSelected()
    }
}

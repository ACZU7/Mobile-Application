package com.example.mobileapplication

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.Assume.assumeTrue
import java.io.File

/** End-to-end checks on the combined build. Screenshots are retained in app-private files. */
class PhaseFourUiTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private fun SemanticsNodeInteraction.reveal(): SemanticsNodeInteraction {
        if (!isDisplayed()) performScrollTo()
        return this
    }
    private fun tap(text: String) = ui.onNodeWithText(text).performClick()
    private fun screenshot(name: String) {
        ui.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val dir = (output?.let { File(it) }
            ?: File(instrumentation.targetContext.filesDir, "phase4-evidence")).apply { mkdirs() }
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        try {
            File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } finally { bitmap.recycle() }
    }

    @Test fun parkingFiltersDetailsAndPermitRemainReachable() {
        tap("Parking")
        ui.onNodeWithText("Nearest car parks").assertExists()
        ui.onNodeWithText("EV charging").reveal().performClick()
        screenshot("01-parking-ev")
        ui.onAllNodes(hasText("Sample", substring = true) and hasClickAction()).onFirst().reveal().performClick()
        screenshot("01b-parking-details-transition")
        ui.onNodeWithText("Parking details").assertIsDisplayed()
        ui.onNodeWithText("Directions").reveal().performClick()
        ui.onNodeWithText("Open in Google Maps?").assertIsDisplayed()
        screenshot("02-directions-confirm")
        tap("Cancel")
        androidx.test.espresso.Espresso.pressBack()
        ui.onNodeWithText("Permit zones").reveal().performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText("Remind me before it expires").fetchSemanticsNodes().isNotEmpty() }
        ui.onNodeWithText("Your resident's permit").reveal().assertIsDisplayed()
        screenshot("03-permit")
    }

    @Test fun streetValidationSubmissionAndStatus() {
        try {
        tap("Streets")
        ui.onNodeWithText("Submit report").reveal().performClick()
        ui.onNodeWithText("Enter a description.").assertExists()
        screenshot("04-street-validation")
        ui.onNodeWithText("Pothole").reveal().performClick()
        ui.onNode(hasSetTextAction() and hasText("Description (required)")).reveal()
            .performTextInput("Fictional Phase 4 regression report near a sample crossing.")
        ui.onNodeWithText("Submit report").reveal().performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText("Report saved locally").fetchSemanticsNodes().isNotEmpty() }
        screenshot("05-street-saved")
        ui.onNodeWithText("View report status").reveal().performClick()
        ui.onNodeWithText("Report status").assertIsDisplayed()
        screenshot("06-street-status")
        } catch (failure: Throwable) { android.util.Log.e("Phase4Failure", "Original test failure", failure); throw failure }
    }

    @Test fun sharedNavigationAndRentIntegration() {
        tap("Housing")
        ui.onNodeWithText("Rent account").reveal().performClick()
        ui.onNodeWithText("Pay rent").reveal().assertIsDisplayed()
        screenshot("07-rent-integration")
        tap("Streets")
        ui.onNodeWithText("My reports").reveal().performClick()
        tap("Fixed")
        ui.onNodeWithText("No local reports match this filter.").assertIsDisplayed()
        screenshot("08-reports-empty")
        tap("Home")
        screenshot("09-home-integration")
    }

    @Test fun deniedPermissionsKeepCoreFlowsUsable() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("permissionMode") == "denied")
        tap("Parking")
        ui.onNodeWithContentDescription("Find my location").performClick()
        ui.onNodeWithText("Location not allowed").assertIsDisplayed()
        screenshot("10-location-denied")
        tap("Understood")
        ui.onNodeWithText("Permit zones").reveal().performClick()
        ui.onNodeWithText("Remind me before it expires").reveal().performClick()
        ui.onNodeWithText("Notifications are off").assertIsDisplayed()
        screenshot("11-notifications-denied")
        tap("Not now")
        tap("Streets")
        ui.onNodeWithText("Add photo").reveal().performClick()
        ui.onNodeWithText("Camera not allowed").assertIsDisplayed()
        screenshot("12-camera-denied")
        tap("Continue without photo")
        ui.onNodeWithText("My reports").assertExists()
    }

    @Test fun cameraCaptureAndRemoval() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("permissionMode") == "allowed")
        tap("Streets")
        ui.onNodeWithText("Add photo").reveal().performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithContentDescription("Take photo").fetchSemanticsNodes().isNotEmpty() }
        // Allow CameraX to bind the emulator camera before exercising its hardware controls.
        Thread.sleep(2000)
        ui.onAllNodesWithContentDescription("Switch camera").fetchSemanticsNodes().firstOrNull()?.let {
            ui.onNodeWithContentDescription("Switch camera").performClick()
            Thread.sleep(2000)
        }
        screenshot("13-camera")
        ui.onNodeWithContentDescription("Take photo").performClick()
        ui.waitUntil(15000) { ui.onAllNodesWithContentDescription("Remove photo 1").fetchSemanticsNodes().isNotEmpty() }
        screenshot("14-photo-attached")
        ui.onNodeWithContentDescription("Remove photo 1").reveal().performClick()
        ui.onNodeWithContentDescription("Remove photo 1").assertDoesNotExist()
    }

    @Test fun permittedLocationSortsResults() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("permissionMode") == "allowed")
        tap("Parking")
        ui.onNodeWithContentDescription("Find my location").performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText("Sorted by straight-line distance from you · fictional sample locations")
            .fetchSemanticsNodes().isNotEmpty() }
        screenshot("15-location-sorted")
    }

    @Test fun permitReminderAcrossReboot() {
        val mode = InstrumentationRegistry.getArguments().getString("reminderMode")
        assumeTrue(mode == "enable" || mode == "verify")
        tap("Parking")
        ui.onNodeWithText("Permit zones").reveal().performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText("Remind me before it expires").fetchSemanticsNodes().isNotEmpty() }
        val toggle = ui.onNodeWithText("Remind me before it expires").reveal()
        if (mode == "enable") toggle.performClick()
        ui.waitUntil(10000) {
            ui.onAllNodes(isToggleable() and isOn()).fetchSemanticsNodes().isNotEmpty()
        }
        toggle.assertIsOn()
        val manager = androidx.work.WorkManager.getInstance(InstrumentationRegistry.getInstrumentation().targetContext)
        val name = "resident_permit_expiry_reminder"
        org.junit.Assert.assertTrue(manager.getWorkInfosForUniqueWork(name).get().any { it.state == androidx.work.WorkInfo.State.ENQUEUED })
        screenshot(if (mode == "enable") "16-reminder-enabled" else "17-reminder-after-reboot")
        if (mode == "verify") {
            toggle.performClick()
            ui.waitUntil(10000) { ui.onAllNodes(isToggleable() and isOff()).fetchSemanticsNodes().isNotEmpty() }
            org.junit.Assert.assertTrue(manager.getWorkInfosForUniqueWork(name).get().none { it.state == androidx.work.WorkInfo.State.ENQUEUED })
        }
    }
}

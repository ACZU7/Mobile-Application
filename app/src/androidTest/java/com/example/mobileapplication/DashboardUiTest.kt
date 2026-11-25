package com.example.mobileapplication

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mobileapplication.data.repository.*
import com.example.mobileapplication.data.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class DashboardUiTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private fun tap(text: String) {
        val node = if (text in listOf("Home", "Parking", "Streets", "Housing", "Admin"))
            ui.onNode(hasText(text) and isSelectable()) else ui.onNodeWithText(text)
        if (!node.isDisplayed()) node.performScrollTo()
        node.performClick()
    }
    private fun screenshot(name: String) {
        ui.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val mode = InstrumentationRegistry.getArguments().getString("evidenceMode")
        val dir = File(context.filesDir, if (mode == "responsive") "final-evidence/responsive" else "final-evidence").apply { mkdirs() }
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    @Test fun staffStatusAndParkingEditsReachResidentScreens() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = StreetReportRepository.getInstance(context)
        val parking = ParkingRepository.getInstance(context)
        repo.load()
        val report = repo.reports.value.first()
        val original = report.status
        val park = parking.carParks.first()
        val desired = if (original == StreetStatus.FIXED) StreetStatus.SUBMITTED else StreetStatus.FIXED
        try {
            tap("Admin")
            ui.waitUntil(10000) { ui.onAllNodesWithText("All statuses").fetchSemanticsNodes().isNotEmpty() }
            screenshot("02-staff-reports")
            // Select just the first report using its current category and status filters.
            val currentLabel = context.getString(original.labelRes)
            ui.onAllNodes(hasText(currentLabel) and hasClickAction()).onFirst().performScrollTo().performClick()
            ui.onNodeWithText(context.getString(desired.labelRes)).performClick()
            ui.onAllNodesWithText("Update").onFirst().performClick()
            ui.waitUntil(10000) { repo.get(report.reference)?.status == desired }
            screenshot("03-status-updated")
            tap("Map"); screenshot("04-staff-map")
            tap("Analytics"); screenshot("05-staff-analytics")
            ui.onNode(hasText("Parking") and hasClickAction() and hasAnyAncestor(hasScrollAction())).performScrollTo().performClick()
            ui.onAllNodesWithText("Edit sample").onFirst().performClick()
            ui.onNodeWithText("Sample spaces (0–10,000)").performTextReplacement("-1")
            tap("Update")
            ui.onNodeWithText("Enter whole spaces from 0 to 10,000 and a price from 0 to 100 with up to two decimal places.").assertExists()
            ui.onNodeWithText("Sample spaces (0–10,000)").performTextReplacement("17")
            ui.onNodeWithText("Sample hourly price (£0–£100)").performTextReplacement("3.25")
            tap("Update")
            ui.waitUntil(10000) { parking.carParks.first().sampleSpaces == 17 }
            ui.waitUntil(10000) { ui.onAllNodesWithText("Edit parking sample").fetchSemanticsNodes().isEmpty() }
            screenshot("06-parking-updated")
            val reopened = ParkingRepository(context.getSharedPreferences("parking_samples", 0))
            assertEquals(17, reopened.carParks.first().sampleSpaces)
            assertEquals(3.25, reopened.carParks.first().sampleHourlyPrice, 0.001)
            tap("Streets"); tap("My reports")
            ui.onAllNodesWithText(context.getString(desired.labelRes)).onFirst().assertExists()
            screenshot("07-resident-status")
            tap("Parking")
            ui.onNodeWithText("17 spaces", substring = true).assertExists()
        } finally {
            repo.updateStatus(report.reference, original)
            parking.updateSample(park.id, park.sampleSpaces, park.sampleHourlyPrice, park.hasEvCharging, park.category)
        }
        Unit
    }
}

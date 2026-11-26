package com.example.mobileapplication

import com.example.mobileapplication.data.model.Role
import org.junit.rules.RuleChain
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

/** Verification only: Housing and CAZ implementation remains owned by Mohammad. */
class PhaseFourSharedServicesTest {
    val ui = createAndroidComposeRule<MainActivity>()
    // Signs in as the demo resident before the activity starts, so the test begins on Home rather than Login.
    @get:Rule val signedIn: RuleChain = RuleChain.outerRule(SignedInRule(Role.RESIDENT)).around(ui)
    private fun text(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
    private fun node(id: Int) = ui.onNodeWithText(text(id))
    private fun SemanticsNodeInteraction.reveal(): SemanticsNodeInteraction {
        if (!isDisplayed()) performScrollTo()
        return this
    }

    @Test fun housingRepairSubmissionAndProgress() {
        ui.onNodeWithText("Housing").performClick()
        node(R.string.housing_form_title).reveal().performClick()
        node(R.string.housing_form_next).performClick()
        node(R.string.housing_error_room).assertExists()
        node(R.string.housing_room_kitchen).reveal().performClick()
        node(R.string.housing_form_next).performClick()
        node(R.string.housing_issue_leak).reveal().performClick()
        ui.onNode(hasSetTextAction()).reveal().performTextInput("Fictional Phase 4 kitchen leak for regression testing.")
        node(R.string.housing_form_next).performClick()
        node(R.string.housing_form_next).performClick()
        node(R.string.housing_appointment_morning).reveal().performClick()
        node(R.string.housing_form_submit).performClick()
        ui.waitUntil(10000) { ui.onAllNodesWithText(text(R.string.housing_confirmation_view_progress))
            .fetchSemanticsNodes().isNotEmpty() }
        node(R.string.housing_confirmation_view_progress).reveal().performClick()
        node(R.string.housing_progress_title).assertIsDisplayed()
        ui.onNodeWithText("Fictional Phase 4 kitchen leak for regression testing.").reveal().assertIsDisplayed()
        node(R.string.housing_progress_demo_label).assertExists()
    }

    @Test fun cazValidationAndEveryOutcomeKeepsPrototypeNotice() {
        node(R.string.home_service_clean_air_zone).reveal().performClick()
        node(R.string.caz_check_button).reveal().performClick()
        node(R.string.caz_error_empty).assertExists()
        val field = ui.onNode(hasSetTextAction())
        for ((plate, result) in listOf("IB15 PTL" to R.string.caz_result_no_charge_title,
            "IC05 PTL" to R.string.caz_result_charge_title, "AB12 CDE" to R.string.caz_result_unknown_title)) {
            field.reveal().performTextReplacement(plate)
            node(R.string.caz_check_button).reveal().performClick()
            node(result).reveal().assertIsDisplayed()
            node(R.string.caz_prototype_notice_title).assertExists()
            node(R.string.caz_prototype_notice_body).assertExists()
            node(R.string.caz_official_checker_button).assertExists()
        }
    }
}

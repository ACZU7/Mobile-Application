package com.example.mobileapplication.ui.cleanairzone

import com.example.mobileapplication.data.model.CazOutcome
import com.example.mobileapplication.data.model.EuroStandard
import com.example.mobileapplication.data.model.FuelType
import com.example.mobileapplication.data.repository.SampleVehicleRepository
import com.example.mobileapplication.domain.cleanairzone.RegistrationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for the events in [CleanAirZoneViewModel]. */
class CleanAirZoneViewModelTest {

    private val viewModel = CleanAirZoneViewModel(SampleVehicleRepository())

    @Test
    fun typing_updatesRegistrationInCapitals() {
        viewModel.onRegistrationChanged("ib15 ptl")

        assertEquals("IB15 PTL", viewModel.state.value.registration)
        assertNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.result)
    }

    @Test
    fun checkingEmptyInput_showsEmptyErrorAndNoResult() {
        viewModel.onCheck()

        assertEquals(RegistrationError.EMPTY, viewModel.state.value.error)
        assertNull(viewModel.state.value.result)
    }

    @Test
    fun checkingInvalidInput_showsErrorAndNoResult() {
        viewModel.onRegistrationChanged("AB-12")
        viewModel.onCheck()

        assertEquals(RegistrationError.INVALID_CHARACTERS, viewModel.state.value.error)
        assertNull(viewModel.state.value.result)
    }

    @Test
    fun typingAfterAnError_clearsTheError() {
        viewModel.onCheck()
        viewModel.onRegistrationChanged("I")

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun checkingDemoPlate_givesItsPrototypeResult() {
        viewModel.onRegistrationChanged("ib15ptl")
        viewModel.onCheck()

        val result = viewModel.state.value.result!!
        assertNull(viewModel.state.value.error)
        assertEquals(CazOutcome.NO_CHARGE, result.outcome)
        assertEquals("Volkswagen", result.make)
        assertEquals("Golf", result.model)
        assertEquals(FuelType.PETROL, result.fuelType)
        assertEquals(EuroStandard.EURO_4, result.euroStandard)
    }

    @Test
    fun checkingDemoPlateThatPays_givesSampleCharge() {
        viewModel.onRegistrationChanged("IC05 PTL")
        viewModel.onCheck()

        val result = viewModel.state.value.result!!
        assertEquals(CazOutcome.CHARGE_APPLIES, result.outcome)
        assertEquals(900, result.dailyChargePence)
    }

    @Test
    fun checkingUnknownPlate_givesUnknownOutcomeNotAnError() {
        viewModel.onRegistrationChanged("AB12 CDE")
        viewModel.onCheck()

        val result = viewModel.state.value.result!!
        assertNull(viewModel.state.value.error)
        assertEquals(CazOutcome.UNKNOWN, result.outcome)
        assertEquals("AB12 CDE", result.registration)
        assertNull(result.make)
        assertNull(result.vehicleType)
    }

    @Test
    fun typingAfterAResult_clearsTheResult() {
        viewModel.onRegistrationChanged("IB15 PTL")
        viewModel.onCheck()
        viewModel.onRegistrationChanged("IB15 PT")

        assertNull(viewModel.state.value.result)
    }

    @Test
    fun officialChecker_opensTheCouncilWebsite() {
        val opened = mutableListOf<String>()

        viewModel.onOpenOfficialChecker { url -> opened += url; true }

        assertEquals(listOf(CleanAirZoneViewModel.OFFICIAL_CHECKER_URL), opened)
        assertNull(viewModel.state.value.officialCheckerFallbackUrl)
    }

    @Test
    fun officialChecker_whenNoAppCanOpenIt_showsTheWebAddressInstead() {
        viewModel.onRegistrationChanged("IB15 PTL")
        viewModel.onCheck()

        viewModel.onOpenOfficialChecker { false }

        val state = viewModel.state.value
        assertEquals(CleanAirZoneViewModel.OFFICIAL_CHECKER_URL, state.officialCheckerFallbackUrl)
        // The result stays on screen under the message.
        assertEquals(CazOutcome.NO_CHARGE, state.result?.outcome)
    }

    @Test
    fun officialChecker_openingLater_clearsTheFallbackMessage() {
        viewModel.onOpenOfficialChecker { false }
        viewModel.onOpenOfficialChecker { true }

        assertNull(viewModel.state.value.officialCheckerFallbackUrl)
    }

    @Test
    fun officialCheckerUrl_isTheCouncilCheckerPage() {
        assertEquals(
            "https://www.bristol.gov.uk/residents/streets-travel/bristols-caz/charges-and-vehicle-checker",
            CleanAirZoneViewModel.OFFICIAL_CHECKER_URL
        )
    }

    @Test
    fun demoRegistrations_comeFromTheRepository() {
        assertEquals(SampleVehicleRepository().registrations, viewModel.demoRegistrations)
    }
}

package com.example.mobileapplication.ui.parking

import com.example.mobileapplication.data.model.ParkingCategory
import com.example.mobileapplication.data.repository.ParkingRepository
import com.example.mobileapplication.testutil.MainDispatcherRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ParkingAdminTest {
    @get:Rule val main = MainDispatcherRule()
    @Test fun editsRefreshExistingFinderAndDetails() {
        val repo = ParkingRepository()
        val vm = ParkingViewModel(repo)
        vm.selectCarPark("sample-01")
        vm.onFinderFilterSelected(ParkingFinderFilter.EV_CHARGING)
        repo.updateSample("sample-01", 17, 3.25, false, ParkingCategory.SURFACE)
        assertFalse(vm.uiState.value.filteredCarParks.any { it.id == "sample-01" })
        assertEquals(17, vm.uiState.value.selectedCarPark?.sampleSpaces)
        assertEquals(3.25, vm.uiState.value.selectedCarPark!!.sampleHourlyPrice, 0.001)
    }
    @Test fun invalidEditsDoNotChangeSample() {
        val repo = ParkingRepository()
        val before = repo.carParks
        listOf(-1 to 2.0, 10001 to 2.0, 1 to Double.NaN, 1 to 101.0).forEach { (spaces, price) ->
            try { repo.updateSample("sample-01", spaces, price, false, ParkingCategory.SURFACE); fail("Accepted invalid value") }
            catch (_: IllegalArgumentException) { }
        }
        assertEquals(before, repo.carParks)
    }
}

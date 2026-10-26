package com.example.mobileapplication.ui.parking

import com.example.mobileapplication.data.model.ParkingArea
import com.example.mobileapplication.data.model.ParkingType
import com.example.mobileapplication.data.repository.ParkingRepository
import org.junit.Assert.*
import org.junit.Test

class ParkingViewModelTest {
    private val repository = ParkingRepository()
    private val viewModel = ParkingViewModel(repository)

    @Test
    fun initialState_exposesEverySample() {
        assertEquals(repository.carParks, viewModel.uiState.value.allCarParks)
        assertEquals(repository.carParks, viewModel.uiState.value.filteredCarParks)
        assertFalse(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun typeFilter_returnsOnlyParkAndRide() {
        viewModel.onTypeSelected(ParkingType.PARK_AND_RIDE)
        assertEquals(listOf("sample-06", "sample-07", "sample-08"), resultIds())
        assertEquals(8, viewModel.uiState.value.allCarParks.size)
    }

    @Test
    fun areaFilter_returnsOnlyCentre() {
        viewModel.onAreaSelected(ParkingArea.CENTRE)
        assertEquals(listOf("sample-01", "sample-02"), resultIds())
    }

    @Test
    fun combinedFilters_matchBothConditions() {
        viewModel.onAreaSelected(ParkingArea.NORTH)
        viewModel.onTypeSelected(ParkingType.PARK_AND_RIDE)
        assertEquals(listOf("sample-06"), resultIds())
    }

    @Test
    fun noMatchingCombination_isEmptyAndClearRestoresAll() {
        viewModel.onTypeSelected(ParkingType.PARK_AND_RIDE)
        viewModel.onAreaSelected(ParkingArea.CENTRE)
        assertTrue(viewModel.uiState.value.isEmpty)
        assertTrue(resultIds().isEmpty())
        viewModel.clearFilters()
        assertEquals(8, resultIds().size)
        assertNull(viewModel.uiState.value.type)
        assertNull(viewModel.uiState.value.area)
    }

    @Test
    fun clearingOneFilter_preservesTheOther() {
        viewModel.onAreaSelected(ParkingArea.CENTRE)
        viewModel.onTypeSelected(ParkingType.PARK_AND_RIDE)
        viewModel.onTypeSelected(null)
        assertEquals(listOf("sample-01", "sample-02"), resultIds())
        assertEquals(ParkingArea.CENTRE, viewModel.uiState.value.area)
    }

    @Test
    fun selection_matchesRepositoryAndSurvivesFilterChanges() {
        viewModel.selectCarPark("sample-03")
        viewModel.onAreaSelected(ParkingArea.SOUTH)
        assertEquals(repository.getCarPark("sample-03"), viewModel.uiState.value.selectedCarPark)
    }

    @Test
    fun invalidSelection_clearsPreviousSelectionAndFallback() {
        viewModel.selectCarPark("sample-01")
        viewModel.showDirectionsMessage()
        viewModel.selectCarPark("missing")
        assertNull(viewModel.uiState.value.selectedCarPark)
        assertFalse(viewModel.uiState.value.directionsMessageVisible)
    }

    @Test
    fun directionsFallback_requiresSelectionAndCanBeDismissed() {
        viewModel.showDirectionsMessage()
        assertFalse(viewModel.uiState.value.directionsMessageVisible)
        viewModel.selectCarPark("sample-01")
        viewModel.showDirectionsMessage()
        assertTrue(viewModel.uiState.value.directionsMessageVisible)
        viewModel.dismissDirectionsMessage()
        assertFalse(viewModel.uiState.value.directionsMessageVisible)
    }

    private fun resultIds() = viewModel.uiState.value.filteredCarParks.map { it.id }

    @Test
    fun finderFilters_matchTheApprovedChipOrder() {
        assertEquals(
            listOf(ParkingFinderFilter.ALL, ParkingFinderFilter.MULTI_STOREY, ParkingFinderFilter.SURFACE, ParkingFinderFilter.EV_CHARGING),
            ParkingFinderFilter.entries
        )
    }

    @Test
    fun multiStoreyFilter_returnsOnlyMatchingSamples() {
        viewModel.onFinderFilterSelected(ParkingFinderFilter.MULTI_STOREY)
        assertEquals(listOf("sample-01", "sample-02", "sample-03"), resultIds())
    }

    @Test
    fun surfaceFilter_includesSurfaceParkAndRideSamples() {
        viewModel.onFinderFilterSelected(ParkingFinderFilter.SURFACE)
        assertEquals(listOf("sample-04", "sample-05", "sample-06", "sample-07", "sample-08"), resultIds())
    }

    @Test
    fun evFilter_returnsChargingSamplesAndCombinesWithArea() {
        viewModel.onFinderFilterSelected(ParkingFinderFilter.EV_CHARGING)
        assertEquals(listOf("sample-01", "sample-04", "sample-06"), resultIds())
        viewModel.onAreaSelected(ParkingArea.EAST)
        assertTrue(viewModel.uiState.value.isEmpty)
        assertNull(viewModel.uiState.value.highlightedCarParkId)
        viewModel.clearFilters()
        assertEquals(8, resultIds().size)
        assertEquals(ParkingFinderFilter.ALL, viewModel.uiState.value.finderFilter)
    }

    @Test
    fun mapSelection_changesHighlightWithoutOpeningDetails() {
        viewModel.highlightCarPark("sample-05")
        assertEquals("sample-05", viewModel.uiState.value.highlightedCarParkId)
        assertNull(viewModel.uiState.value.selectedCarPark)
        viewModel.onFinderFilterSelected(ParkingFinderFilter.MULTI_STOREY)
        assertEquals("sample-01", viewModel.uiState.value.highlightedCarParkId)
        viewModel.highlightCarPark("sample-05")
        assertEquals("sample-01", viewModel.uiState.value.highlightedCarParkId)
    }

    @Test
    fun locationFallback_canBeShownAndDismissedWithoutSelectingAPark() {
        viewModel.setLocationMessageVisible(true)
        assertTrue(viewModel.uiState.value.locationMessageVisible)
        assertNull(viewModel.uiState.value.selectedCarPark)
        viewModel.setLocationMessageVisible(false)
        assertFalse(viewModel.uiState.value.locationMessageVisible)
    }
}

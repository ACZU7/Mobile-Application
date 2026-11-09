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
    fun invalidSelection_clearsPreviousSelectionAndDirectionsDialogs() {
        viewModel.selectCarPark("sample-01")
        viewModel.requestDirections()
        viewModel.selectCarPark("missing")
        assertNull(viewModel.uiState.value.selectedCarPark)
        assertFalse(viewModel.uiState.value.directionsConfirmVisible)
        assertFalse(viewModel.uiState.value.directionsUnavailableVisible)
    }

    @Test
    fun directionsConfirmation_requiresSelectionAndCanBeCancelled() {
        viewModel.requestDirections()
        assertFalse(viewModel.uiState.value.directionsConfirmVisible)
        viewModel.selectCarPark("sample-01")
        viewModel.requestDirections()
        assertTrue(viewModel.uiState.value.directionsConfirmVisible)
        viewModel.dismissDirections()
        assertFalse(viewModel.uiState.value.directionsConfirmVisible)
    }

    @Test
    fun directionsOpened_closesConfirmationWithoutFallback() {
        viewModel.selectCarPark("sample-02")
        viewModel.requestDirections()
        viewModel.onDirectionsLaunched(opened = true)
        assertFalse(viewModel.uiState.value.directionsConfirmVisible)
        assertFalse(viewModel.uiState.value.directionsUnavailableVisible)
    }

    @Test
    fun noMapsAppOrBrowser_showsFallbackThatCanBeDismissed() {
        viewModel.selectCarPark("sample-02")
        viewModel.requestDirections()
        viewModel.onDirectionsLaunched(opened = false)
        assertFalse(viewModel.uiState.value.directionsConfirmVisible)
        assertTrue(viewModel.uiState.value.directionsUnavailableVisible)
        viewModel.dismissDirectionsUnavailable()
        assertFalse(viewModel.uiState.value.directionsUnavailableVisible)
        assertEquals(repository.getCarPark("sample-02"), viewModel.uiState.value.selectedCarPark)
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
    fun locating_showsProgressWithoutChangingOrder() {
        viewModel.onLocating()
        assertEquals(LocationStatus.LOCATING, viewModel.uiState.value.locationStatus)
        assertEquals(repository.carParks.map { it.id }, resultIds())
    }

    @Test
    fun locationReceived_sortsNearestFirstAndHighlightsNearest() {
        viewModel.onLocating()
        viewModel.onLocationReceived(EAST_PARK_AND_RIDE)
        val state = viewModel.uiState.value
        assertEquals(LocationStatus.SORTED_BY_DISTANCE, state.locationStatus)
        assertEquals("sample-08", resultIds().first())
        assertEquals("sample-08", state.highlightedCarParkId)
        val distances = state.filteredCarParks.map { state.distanceMiles(it) }
        assertEquals(distances.sorted(), distances)
        assertEquals(0.0, distances.first(), 0.001)
        assertNull(state.locationMessage)
        assertEquals(1, state.distanceSortVersion)
    }

    @Test
    fun filterChanges_doNotCountAsANewDistanceSort() {
        viewModel.onLocationReceived(EAST_PARK_AND_RIDE)
        viewModel.onTypeSelected(ParkingType.CAR_PARK)
        assertEquals(1, viewModel.uiState.value.distanceSortVersion)
        viewModel.onLocationReceived(GeoPoint(51.454, -2.593))
        assertEquals(2, viewModel.uiState.value.distanceSortVersion)
        assertEquals("sample-01", resultIds().first())
    }

    @Test
    fun distanceSorting_isKeptWhenFiltersChange() {
        viewModel.onLocationReceived(EAST_PARK_AND_RIDE)
        viewModel.onTypeSelected(ParkingType.PARK_AND_RIDE)
        assertEquals(listOf("sample-08", "sample-06", "sample-07"), resultIds())
        viewModel.clearFilters()
        assertEquals(8, resultIds().size)
        assertEquals("sample-08", resultIds().first())
    }

    @Test
    fun withoutLocation_rowsUseSampleDistances() {
        val state = viewModel.uiState.value
        val first = state.filteredCarParks.first()
        assertEquals(first.sampleDistanceMiles, state.distanceMiles(first), 0.0)
    }

    @Test
    fun locationDenied_keepsDefaultOrderAndExplains() {
        viewModel.onLocating()
        viewModel.onLocationDenied()
        val state = viewModel.uiState.value
        assertEquals(LocationStatus.IDLE, state.locationStatus)
        assertEquals(LocationMessage.DENIED, state.locationMessage)
        assertEquals(repository.carParks.map { it.id }, resultIds())
        assertNull(state.selectedCarPark)
        viewModel.dismissLocationMessage()
        assertNull(viewModel.uiState.value.locationMessage)
    }

    @Test
    fun locationUnavailable_explainsAndKeepsAnEarlierSort() {
        viewModel.onLocationReceived(EAST_PARK_AND_RIDE)
        viewModel.onLocating()
        viewModel.onLocationUnavailable()
        val state = viewModel.uiState.value
        assertEquals(LocationMessage.UNAVAILABLE, state.locationMessage)
        assertEquals(LocationStatus.SORTED_BY_DISTANCE, state.locationStatus)
        assertEquals("sample-08", resultIds().first())
    }

    private companion object {
        /** Same coordinates as sample-08, so it must sort first at zero miles. */
        val EAST_PARK_AND_RIDE = GeoPoint(51.47, -2.52)
    }
}

package com.example.mobileapplication.ui.parking

import androidx.lifecycle.ViewModel
import com.example.mobileapplication.data.model.CarPark
import com.example.mobileapplication.data.model.ParkingCategory
import com.example.mobileapplication.data.model.ParkingArea
import com.example.mobileapplication.data.model.ParkingType
import com.example.mobileapplication.data.repository.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.mobileapplication.R

enum class ParkingFinderFilter(val labelRes: Int) {
    ALL(R.string.parking_filter_all),
    MULTI_STOREY(R.string.parking_multi_storey),
    SURFACE(R.string.parking_surface),
    EV_CHARGING(R.string.parking_ev_charging)
}

/** Progress of the user's optional "Find my location" request. */
enum class LocationStatus { IDLE, LOCATING, SORTED_BY_DISTANCE }

/** Explanation shown when the list cannot be sorted by distance. */
enum class LocationMessage { DENIED, UNAVAILABLE }

data class ParkingUiState(
    val allCarParks: List<CarPark>,
    val filteredCarParks: List<CarPark> = allCarParks,
    val type: ParkingType? = null,
    val area: ParkingArea? = null,
    val selectedCarPark: CarPark? = null,
    val directionsConfirmVisible: Boolean = false,
    val directionsUnavailableVisible: Boolean = false,
    val finderFilter: ParkingFinderFilter = ParkingFinderFilter.ALL,
    val highlightedCarParkId: String? = allCarParks.firstOrNull()?.id,
    val filtersVisible: Boolean = false,
    val userLocation: GeoPoint? = null,
    val distancesMiles: Map<String, Double> = emptyMap(),
    val locationStatus: LocationStatus = LocationStatus.IDLE,
    val locationMessage: LocationMessage? = null,
    /** Increases each time a new location re-sorts the list, so the screen can scroll back to the nearest result. */
    val distanceSortVersion: Int = 0
) {
    val isEmpty: Boolean get() = filteredCarParks.isEmpty()

    /** Real distance from the user when known, otherwise the fixed sample distance. */
    fun distanceMiles(carPark: CarPark): Double = distancesMiles[carPark.id] ?: carPark.sampleDistanceMiles
}

/** Owns filters, selection, directions hand-off and optional distance sorting for the Parking graph. */
class ParkingViewModel(private val repository: ParkingRepository = ParkingRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(ParkingUiState(repository.carParks))
    val uiState = _uiState.asStateFlow()

    fun onTypeSelected(type: ParkingType?) {
        _uiState.update { filter(it.copy(type = type)) }
    }

    fun onAreaSelected(area: ParkingArea?) {
        _uiState.update { filter(it.copy(area = area)) }
    }

    fun clearFilters() {
        _uiState.update { filter(it.copy(type = null, area = null, finderFilter = ParkingFinderFilter.ALL)) }
    }

    fun onFinderFilterSelected(filter: ParkingFinderFilter) {
        _uiState.update { filter(it.copy(finderFilter = filter)) }
    }

    fun highlightCarPark(id: String) {
        _uiState.update { state ->
            if (state.filteredCarParks.any { it.id == id }) state.copy(highlightedCarParkId = id) else state
        }
    }

    fun setFiltersVisible(visible: Boolean) {
        _uiState.update { it.copy(filtersVisible = visible) }
    }

    fun selectCarPark(id: String) {
        _uiState.update {
            it.copy(
                selectedCarPark = repository.getCarPark(id),
                directionsConfirmVisible = false,
                directionsUnavailableVisible = false
            )
        }
    }

    /** Directions first asks the user to confirm, because the coordinates are fictional. */
    fun requestDirections() {
        _uiState.update { it.copy(directionsConfirmVisible = it.selectedCarPark != null) }
    }

    fun dismissDirections() {
        _uiState.update { it.copy(directionsConfirmVisible = false) }
    }

    /** Called after trying to open a maps app; [opened] is false when nothing on the phone could. */
    fun onDirectionsLaunched(opened: Boolean) {
        _uiState.update { it.copy(directionsConfirmVisible = false, directionsUnavailableVisible = !opened) }
    }

    fun dismissDirectionsUnavailable() {
        _uiState.update { it.copy(directionsUnavailableVisible = false) }
    }

    fun onLocating() {
        _uiState.update { it.copy(locationStatus = LocationStatus.LOCATING, locationMessage = null) }
    }

    /** Sorts the list nearest-first and highlights the nearest result. */
    fun onLocationReceived(location: GeoPoint) {
        _uiState.update { state ->
            val distances = state.allCarParks.associate {
                it.id to DistanceCalculator.miles(location, GeoPoint(it.latitude, it.longitude))
            }
            val sorted = filter(
                state.copy(
                    userLocation = location,
                    distancesMiles = distances,
                    locationStatus = LocationStatus.SORTED_BY_DISTANCE,
                    locationMessage = null,
                    distanceSortVersion = state.distanceSortVersion + 1
                )
            )
            sorted.copy(highlightedCarParkId = sorted.filteredCarParks.firstOrNull()?.id)
        }
    }

    /** Permission refused: keep the current list and order, and explain the alternative. */
    fun onLocationDenied() {
        _uiState.update { it.copy(locationStatus = idleOrSorted(it), locationMessage = LocationMessage.DENIED) }
    }

    /** Permission granted but no position could be read (for example, location turned off). */
    fun onLocationUnavailable() {
        _uiState.update { it.copy(locationStatus = idleOrSorted(it), locationMessage = LocationMessage.UNAVAILABLE) }
    }

    fun dismissLocationMessage() {
        _uiState.update { it.copy(locationMessage = null) }
    }

    private fun idleOrSorted(state: ParkingUiState) =
        if (state.userLocation != null) LocationStatus.SORTED_BY_DISTANCE else LocationStatus.IDLE

    private fun filter(state: ParkingUiState): ParkingUiState {
        val matches = state.allCarParks.filter {
            (state.type == null || it.type == state.type) &&
                (state.area == null || it.area == state.area) && when (state.finderFilter) {
                    ParkingFinderFilter.ALL -> true
                    ParkingFinderFilter.MULTI_STOREY -> it.category == ParkingCategory.MULTI_STOREY
                    ParkingFinderFilter.SURFACE -> it.category == ParkingCategory.SURFACE
                    ParkingFinderFilter.EV_CHARGING -> it.hasEvCharging
                }
        }
        val results = if (state.userLocation != null) matches.sortedBy { state.distanceMiles(it) } else matches
        return state.copy(
            filteredCarParks = results,
            highlightedCarParkId = state.highlightedCarParkId?.takeIf { id -> results.any { it.id == id } }
                ?: results.firstOrNull()?.id
        )
    }
}

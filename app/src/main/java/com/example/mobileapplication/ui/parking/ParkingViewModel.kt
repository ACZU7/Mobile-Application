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

data class ParkingUiState(
    val allCarParks: List<CarPark>,
    val filteredCarParks: List<CarPark> = allCarParks,
    val type: ParkingType? = null,
    val area: ParkingArea? = null,
    val selectedCarPark: CarPark? = null,
    val directionsMessageVisible: Boolean = false,
    val finderFilter: ParkingFinderFilter = ParkingFinderFilter.ALL,
    val highlightedCarParkId: String? = allCarParks.firstOrNull()?.id,
    val filtersVisible: Boolean = false,
    val locationMessageVisible: Boolean = false
) {
    val isEmpty: Boolean get() = filteredCarParks.isEmpty()
}

/** Owns filters, selection and the Phase 1 directions fallback for the Parking graph. */
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

    fun setLocationMessageVisible(visible: Boolean) {
        _uiState.update { it.copy(locationMessageVisible = visible) }
    }

    fun selectCarPark(id: String) {
        _uiState.update { it.copy(selectedCarPark = repository.getCarPark(id), directionsMessageVisible = false) }
    }

    fun showDirectionsMessage() {
        _uiState.update { it.copy(directionsMessageVisible = it.selectedCarPark != null) }
    }

    fun dismissDirectionsMessage() {
        _uiState.update { it.copy(directionsMessageVisible = false) }
    }

    private fun filter(state: ParkingUiState): ParkingUiState {
        val results = state.allCarParks.filter {
            (state.type == null || it.type == state.type) &&
                (state.area == null || it.area == state.area) && when (state.finderFilter) {
                    ParkingFinderFilter.ALL -> true
                    ParkingFinderFilter.MULTI_STOREY -> it.category == ParkingCategory.MULTI_STOREY
                    ParkingFinderFilter.SURFACE -> it.category == ParkingCategory.SURFACE
                    ParkingFinderFilter.EV_CHARGING -> it.hasEvCharging
                }
        }
        return state.copy(
            filteredCarParks = results,
            highlightedCarParkId = state.highlightedCarParkId?.takeIf { id -> results.any { it.id == id } }
                ?: results.firstOrNull()?.id
        )
    }
}

package com.example.mobileapplication.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.UserProfile
import com.example.mobileapplication.data.repository.DataStoreProfileRepository
import com.example.mobileapplication.data.repository.ProfileRepository
import com.example.mobileapplication.data.repository.RepairRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalTime

/**
 * Everything the Home dashboard shows.
 *
 * @property isLoaded False until the profile has been read, so the header does
 * not flash "Welcome" before the name arrives.
 * @property timeOfDay Picks "Good morning", "Good afternoon" or "Good evening".
 * @property firstName The profile's first name, or null to show "Welcome".
 * @property services The tiles that match the search text, in grid order.
 * @property latestRepair The newest repair request in the database, or null if there are none.
 */
data class HomeUiState(
    val isLoaded: Boolean = false,
    val timeOfDay: TimeOfDay = TimeOfDay.MORNING,
    val firstName: String? = null,
    val services: List<HomeService> = HomeService.entries,
    val latestRepair: RepairRequest? = null
)

/**
 * The ViewModel for the Home dashboard (the "VM" in MVVM).
 *
 * It combines the saved profile (for the greeting), the search text (to filter
 * the tiles) and the newest repair request from Room (for the "Your latest
 * repair" card).
 *
 * @param profileRepository The local demo profile.
 * @param repairRequests Every repair request, newest first.
 * @param searchableText The words a tile can be found by: its label and keywords.
 * It is a function so tests do not need Android resources.
 * @param now The current time, replaceable in tests.
 */
class HomeViewModel(
    profileRepository: ProfileRepository,
    repairRequests: Flow<List<RepairRequest>>,
    private val searchableText: (HomeService) -> String,
    private val now: () -> LocalTime = { LocalTime.now() }
) : ViewModel() {

    private val query = MutableStateFlow("")

    /**
     * What is typed in the search field. Kept apart from [uiState] so the text
     * field updates straight away, without waiting for the combined state.
     */
    val searchText: StateFlow<String> = query.asStateFlow()

    /** The state of the Home screen. */
    val uiState: StateFlow<HomeUiState> = combine(
        profileRepository.profile.catchAsEmpty(UserProfile()),
        repairRequests.map { it.firstOrNull() }.catchAsEmpty(null),
        query
    ) { profile, latest, text ->
        HomeUiState(
            isLoaded = true,
            timeOfDay = TimeOfDay.fromHour(now().hour),
            firstName = profile.firstName,
            services = HomeService.entries.filter { HomeSearch.matches(text, searchableText(it)) },
            latestRepair = latest
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        HomeUiState(timeOfDay = TimeOfDay.fromHour(now().hour))
    )

    /** Called every time the search text changes. */
    fun onQueryChanged(text: String) {
        query.value = text
    }

    /** Called when the user taps the clear (X) button in the search field. */
    fun onClearQuery() {
        query.value = ""
    }

    /** Creates the ViewModel with the app's DataStore profile and Room repairs. */
    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        /** Factory used by `viewModel(factory = HomeViewModel.Factory)`. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app: Application = this[APPLICATION_KEY]!!
                HomeViewModel(
                    profileRepository = DataStoreProfileRepository(app),
                    repairRequests = RepairRepository.getInstance(app).requests,
                    searchableText = { service ->
                        app.getString(service.labelRes) + " " + app.getString(service.keywordsRes)
                    }
                )
            }
        }
    }
}

/** If reading fails, Home still shows the tiles instead of crashing; the card or name is just left out. */
private fun <T> Flow<T>.catchAsEmpty(fallback: T): Flow<T> = catch { error ->
    if (error is CancellationException) throw error
    emit(fallback)
}

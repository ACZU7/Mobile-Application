package com.example.mobileapplication.ui.home

import com.example.mobileapplication.data.local.toEntity
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.UserProfile
import com.example.mobileapplication.data.repository.RepairRepository
import com.example.mobileapplication.testutil.FakeProfileRepository
import com.example.mobileapplication.testutil.FakeRepairRequestDao
import com.example.mobileapplication.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * Unit tests for [HomeViewModel]: greeting, search filtering and the latest repair card.
 * The profile and repairs come from in-memory fakes, so no device is needed.
 */
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** English labels and keywords, standing in for `strings.xml`. */
    private val searchableText: (HomeService) -> String = {
        when (it) {
            HomeService.FIND_PARKING -> "Find parking car park spaces"
            HomeService.PERMIT_ZONES -> "Permit zones resident permit"
            HomeService.REPORT_ISSUE -> "Report an issue pothole graffiti"
            HomeService.CLEAN_AIR_ZONE -> "Clean Air Zone caz vehicle"
            HomeService.REPORT_REPAIR -> "Report a repair housing leak"
            HomeService.RENT_ACCOUNT -> "Rent account payment balance"
        }
    }

    private val today = LocalDate.of(2026, 11, 25)

    private fun viewModel(
        profile: UserProfile = UserProfile(),
        dao: FakeRepairRequestDao = FakeRepairRequestDao(),
        time: LocalTime = LocalTime.of(9, 0)
    ) = HomeViewModel(
        profileRepository = FakeProfileRepository(profile),
        repairRequests = RepairRepository(dao, today = { today }).requests,
        searchableText = searchableText,
        now = { time }
    )

    /** Keeps [HomeViewModel.uiState] subscribed, as the screen would. */
    private fun TestScope.observe(viewModel: HomeViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
    }

    @Test
    fun greeting_usesTheProfileFirstNameAndTimeOfDay() = runTest {
        val vm = viewModel(profile = UserProfile(name = "Sam Wilson"), time = LocalTime.of(8, 30))
        observe(vm)

        assertTrue(vm.uiState.value.isLoaded)
        assertEquals("Sam", vm.uiState.value.firstName)
        assertEquals(TimeOfDay.MORNING, vm.uiState.value.timeOfDay)
    }

    @Test
    fun greeting_withoutAName_hasNoFirstNameSoTheScreenSaysWelcome() = runTest {
        val vm = viewModel(time = LocalTime.of(19, 0))
        observe(vm)

        assertNull(vm.uiState.value.firstName)
        assertEquals(TimeOfDay.EVENING, vm.uiState.value.timeOfDay)
    }

    @Test
    fun beforeLoading_theStateIsNotLoaded() {
        assertFalse(viewModel().uiState.value.isLoaded)
    }

    @Test
    fun timeOfDay_boundaries() {
        assertEquals(TimeOfDay.EVENING, TimeOfDay.fromHour(4))
        assertEquals(TimeOfDay.MORNING, TimeOfDay.fromHour(5))
        assertEquals(TimeOfDay.MORNING, TimeOfDay.fromHour(11))
        assertEquals(TimeOfDay.AFTERNOON, TimeOfDay.fromHour(12))
        assertEquals(TimeOfDay.AFTERNOON, TimeOfDay.fromHour(17))
        assertEquals(TimeOfDay.EVENING, TimeOfDay.fromHour(18))
    }

    @Test
    fun noSearch_showsAllSixTilesInOrder() = runTest {
        val vm = viewModel()
        observe(vm)

        assertEquals(HomeService.entries, vm.uiState.value.services)
    }

    @Test
    fun search_filtersTilesAsYouType() = runTest {
        val vm = viewModel()
        observe(vm)

        vm.onQueryChanged("rep")
        assertEquals(listOf(HomeService.REPORT_ISSUE, HomeService.REPORT_REPAIR), vm.uiState.value.services)

        vm.onQueryChanged("repair")
        assertEquals(listOf(HomeService.REPORT_REPAIR), vm.uiState.value.services)
        assertEquals("repair", vm.searchText.value)
    }

    @Test
    fun search_matchesKeywordsAndIgnoresCase() = runTest {
        val vm = viewModel()
        observe(vm)

        vm.onQueryChanged("POTHOLE")

        assertEquals(listOf(HomeService.REPORT_ISSUE), vm.uiState.value.services)
    }

    @Test
    fun search_withNoMatch_isEmpty_andClearingShowsAllAgain() = runTest {
        val vm = viewModel()
        observe(vm)

        vm.onQueryChanged("library")
        assertTrue(vm.uiState.value.services.isEmpty())

        vm.onClearQuery()
        assertEquals("", vm.searchText.value)
        assertEquals(HomeService.entries, vm.uiState.value.services)
    }

    @Test
    fun homeSearch_needsEveryWord() {
        assertTrue(HomeSearch.matches("car park", "Find parking car park"))
        assertFalse(HomeSearch.matches("car rent", "Find parking car park"))
        assertTrue(HomeSearch.matches("   ", "anything"))
    }

    @Test
    fun latestRepair_isTheNewestRequestInTheDatabase() = runTest {
        val vm = viewModel()
        observe(vm)

        // The repository seeds three samples; HR-2026-0003 (15 Oct) is the newest.
        assertEquals("HR-2026-0003", vm.uiState.value.latestRepair?.reference)
    }

    @Test
    fun latestRepair_updatesWhenANewRequestIsSaved() = runTest {
        val dao = FakeRepairRequestDao(RepairRepository.sampleRequests().map { it.toEntity() })
        val vm = viewModel(dao = dao)
        observe(vm)

        val newRequest = RepairRepository.sampleRequests().first()
            .copy(reference = "HR-2026-0004", createdDate = today, isSample = false)
        dao.insert(newRequest.toEntity())

        assertEquals("HR-2026-0004", vm.uiState.value.latestRepair?.reference)
    }

    @Test
    fun latestRepair_isHiddenWhenThereAreNone() = runTest {
        val vm = HomeViewModel(FakeProfileRepository(), flowOf(emptyList()), searchableText, { LocalTime.NOON })
        observe(vm)

        assertTrue(vm.uiState.value.isLoaded)
        assertNull(vm.uiState.value.latestRepair)
    }

    @Test
    fun databaseError_stillShowsTheTiles() = runTest {
        val failing = flow<List<RepairRequest>> {
            throw IllegalStateException("Simulated database failure")
        }
        val vm = HomeViewModel(FakeProfileRepository(), failing, searchableText, { LocalTime.NOON })
        observe(vm)

        assertTrue(vm.uiState.value.isLoaded)
        assertNull(vm.uiState.value.latestRepair)
        assertEquals(HomeService.entries, vm.uiState.value.services)
    }
}

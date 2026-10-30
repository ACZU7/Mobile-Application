package com.example.mobileapplication.ui.streetissues

import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.*
import com.example.mobileapplication.data.repository.StreetReportRepository
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class StreetIssuesTest {
    private fun repository(samples: Boolean = true) = StreetReportRepository({ LocalDate.of(2026, 10, 30) }, samples)
    private fun valid() = StreetFormState(StreetCategory.POTHOLE, "A fictional pothole by the crossing.")
    @Test fun categoryRequired() { assertEquals(R.string.street_error_category, StreetReportValidator.validate(valid().copy(category = null)).categoryError) }
    @Test fun descriptionRequired() { assertEquals(R.string.street_error_required, StreetReportValidator.validate(valid().copy(description = "  ")).descriptionError) }
    @Test fun minimumLength() {
        assertEquals(R.string.street_error_short, StreetReportValidator.validate(valid().copy(description = "123456789")).descriptionError)
        assertTrue(StreetReportValidator.validate(valid().copy(description = "1234567890")).isValid)
    }
    @Test fun maximumLength() {
        assertTrue(StreetReportValidator.validate(valid().copy(description = "x".repeat(500))).isValid)
        assertEquals(R.string.street_error_long, StreetReportValidator.validate(valid().copy(description = "x".repeat(501))).descriptionError)
    }
    @Test fun minimumIgnoresWhitespace() { assertFalse(StreetReportValidator.validate(valid().copy(description = "  short  ")).isValid) }
    @Test fun blankLocationRejected() { assertEquals(R.string.street_error_location, StreetReportValidator.validate(valid().copy(locationLabel = " ")).locationError) }
    @Test fun referenceFormat() { assertEquals("SR-2026-0004", StreetReportRepository.formatReference(2026, 4)) }
    @Test fun saveRetrieveAndUniqueReferences() {
        val repo = repository()
        val first = repo.save(StreetCategory.GRAFFITI, " Fictional graffiti on wall ", "Demo road")
        val second = repo.save(StreetCategory.POTHOLE, "Fictional road damage", "Demo road")
        assertEquals("SR-2026-0004", first.reference)
        assertEquals("SR-2026-0005", second.reference)
        assertEquals(first, repo.get(first.reference))
        assertEquals("Fictional graffiti on wall", first.description)
        assertEquals(StreetStatus.SUBMITTED, first.status)
        assertNull(repo.get("missing"))
        assertEquals(second, repo.reports.value.first())
    }
    @Test fun validSubmission() {
        val repo = repository(); val vm = StreetIssuesViewModel(repo)
        vm.category(StreetCategory.FLY_TIPPING); vm.description("  Fictional dumped rubbish  "); vm.location("Demo lane")
        assertTrue(vm.submit())
        assertEquals("SR-2026-0004", vm.selectedReport.value?.reference)
        assertEquals("Demo lane", vm.selectedReport.value?.locationLabel)
        assertEquals("Fictional dumped rubbish", vm.selectedReport.value?.description)
        assertEquals(StreetFormState(), vm.formState.value)
        assertEquals(3, repo.reports.value.size)
    }
    @Test fun invalidSubmissionSavesNothingAndCanRecover() {
        val repo = repository(); val vm = StreetIssuesViewModel(repo)
        assertFalse(vm.submit()); assertEquals(2, repo.reports.value.size); assertNull(vm.selectedReport.value)
        assertNotNull(vm.formState.value.categoryError)
        vm.category(StreetCategory.POTHOLE); vm.description("Fictional pothole at crossing")
        assertNull(vm.formState.value.categoryError); assertNull(vm.formState.value.descriptionError)
        assertTrue(vm.submit())
    }
    @Test fun overlongSubmissionPreservesInput() {
        val repo = repository(); val vm = StreetIssuesViewModel(repo)
        vm.category(StreetCategory.POTHOLE); vm.description("x".repeat(501))
        assertFalse(vm.submit()); assertEquals(501, vm.formState.value.description.length); assertEquals(2, repo.reports.value.size)
    }
    @Test fun filtersEveryStatusAndAll() {
        val reports = repository().reports.value
        assertEquals(reports, StreetIssuesViewModel.filtered(reports, null))
        StreetStatus.entries.forEach { status -> assertTrue(StreetIssuesViewModel.filtered(reports, status).all { it.status == status }) }
        assertEquals(1, StreetIssuesViewModel.filtered(reports, StreetStatus.IN_PROGRESS).size)
    }
    @Test fun emptyFilterResults() { assertTrue(StreetIssuesViewModel.filtered(repository().reports.value, StreetStatus.FIXED).isEmpty()) }
    @Test fun emptyRepository() { assertTrue(StreetIssuesViewModel.filtered(repository(false).reports.value, null).isEmpty()) }
    @Test fun filterAndSelectionEvents() {
        val repo = repository(); val vm = StreetIssuesViewModel(repo)
        vm.filter(StreetStatus.IN_PROGRESS); assertEquals(StreetStatus.IN_PROGRESS, vm.filter.value)
        vm.filter(null); assertNull(vm.filter.value)
        vm.select("SR-2026-0003"); assertEquals(repo.get("SR-2026-0003"), vm.selectedReport.value)
    }
}

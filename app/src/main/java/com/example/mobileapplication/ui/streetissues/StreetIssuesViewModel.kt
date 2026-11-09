package com.example.mobileapplication.ui.streetissues

import androidx.lifecycle.ViewModel
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.*
import com.example.mobileapplication.data.repository.StreetReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

const val STREET_DESCRIPTION_MAX = 500
const val STREET_MAX_PHOTOS = 3

data class StreetFormState(
    val category: StreetCategory? = null,
    val description: String = "",
    val locationLabel: String = "Demo Nelson Street, Bristol BS1 2JY",
    /** Optional; the report can always be submitted without a photo. */
    val photos: List<String> = emptyList(),
    val categoryError: Int? = null,
    val descriptionError: Int? = null,
    val locationError: Int? = null
) {
    val isValid get() = categoryError == null && descriptionError == null && locationError == null
    val canAddPhoto get() = photos.size < STREET_MAX_PHOTOS
}
object StreetReportValidator {
    fun validate(form: StreetFormState) = form.copy(
        categoryError = if (form.category == null) R.string.street_error_category else null,
        descriptionError = when {
            form.description.isBlank() -> R.string.street_error_required
            form.description.trim().length < 10 -> R.string.street_error_short
            form.description.length > STREET_DESCRIPTION_MAX -> R.string.street_error_long
            else -> null
        },
        locationError = if (form.locationLabel.isBlank()) R.string.street_error_location else null
    )
}
class StreetIssuesViewModel(private val repository: StreetReportRepository = StreetReportRepository.shared) : ViewModel() {
    private val form = MutableStateFlow(StreetFormState())
    val formState = form.asStateFlow()
    val reports = repository.reports
    private val selected = MutableStateFlow<StreetReport?>(null)
    val selectedReport = selected.asStateFlow()
    private val statusFilter = MutableStateFlow<StreetStatus?>(null)
    val filter = statusFilter.asStateFlow()
    fun category(value: StreetCategory) { form.update { it.copy(category = value, categoryError = null) } }
    fun description(value: String) { form.update { it.copy(description = value, descriptionError = null) } }
    fun location(value: String) { form.update { it.copy(locationLabel = value, locationError = null) } }
    fun filter(value: StreetStatus?) { statusFilter.value = value }
    fun addPhoto(uri: String) { form.update { if (it.canAddPhoto && uri !in it.photos) it.copy(photos = it.photos + uri) else it } }
    fun removePhoto(uri: String) { form.update { it.copy(photos = it.photos - uri) } }
    fun select(reference: String) { selected.value = repository.get(reference) }
    fun submit(): Boolean {
        val checked = StreetReportValidator.validate(form.value)
        form.value = checked
        if (!checked.isValid) return false
        selected.value = repository.save(checked.category!!, checked.description, checked.locationLabel, checked.photos)
        form.value = StreetFormState()
        return true
    }
    companion object {
        fun filtered(reports: List<StreetReport>, status: StreetStatus?) = reports.filter { status == null || it.status == status }
    }
}

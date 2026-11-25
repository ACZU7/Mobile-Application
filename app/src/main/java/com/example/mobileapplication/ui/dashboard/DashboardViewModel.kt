package com.example.mobileapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapplication.data.model.*
import com.example.mobileapplication.data.repository.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(val streets: StreetReportRepository, val parking: ParkingRepository) : ViewModel() {
    private val failed = MutableStateFlow(false)
    val error = failed.asStateFlow()
    private val loading = MutableStateFlow(true)
    val busy = loading.asStateFlow()
    private val notice = MutableStateFlow(false)
    val saved = notice.asStateFlow()
    val reports = streets.reports
    val carParks = parking.changes
    init { reload() }
    fun dismissNotice() { notice.value = false }
    fun reload() = runAction { streets.load() }
    fun updateStatus(reference: String, status: StreetStatus) = runAction {
        streets.updateStatus(reference, status); notice.value = true
    }
    fun updateParking(id: String, spaces: Int, price: Double, ev: Boolean, category: ParkingCategory) = runAction {
        withContext(Dispatchers.IO) { parking.updateSample(id, spaces, price, ev, category) }
        notice.value = true
    }
    private fun runAction(action: suspend () -> Unit) = viewModelScope.launch {
        loading.value = true; failed.value = false; notice.value = false
        try { action() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { failed.value = true }
        finally { loading.value = false }
    }
}

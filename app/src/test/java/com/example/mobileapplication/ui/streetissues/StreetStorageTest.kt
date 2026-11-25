package com.example.mobileapplication.ui.streetissues

import com.example.mobileapplication.data.local.*
import com.example.mobileapplication.data.model.*
import com.example.mobileapplication.data.repository.StreetReportRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class StreetStorageTest {
    private class FakeDao : StreetReportDao {
        val rows = mutableListOf<StreetReportEntity>()
        var failWrites = false
        override suspend fun updateStatus(reference: String, status: String): Int {
            if (failWrites) throw IOException("Disk full")
            val index = rows.indexOfFirst { it.reference == reference }
            if (index < 0) return 0
            rows[index] = rows[index].copy(status = status)
            return 1
        }
        override suspend fun getAll() = rows.toList()
        override suspend fun insert(report: StreetReportEntity) {
            if (failWrites) throw IOException("Disk full")
            check(rows.none { it.reference == report.reference })
            rows.add(report)
        }
        override suspend fun insertSamples(reports: List<StreetReportEntity>) { rows.addAll(reports) }
    }

    @Test fun reloadPreservesAllFieldsAndDoesNotReseed() = runTest {
        val dao = FakeDao()
        val first = StreetReportRepository(dao = dao)
        val saved = first.save(StreetCategory.GRAFFITI, "Fictional wall damage", "Demo lane",
            listOf("file:///photo,one.jpg", "file:///صورة\n2.jpg"))
        val reopened = StreetReportRepository(dao = dao)
        reopened.load()
        assertEquals(saved, reopened.get(saved.reference))
        assertEquals(3, reopened.reports.value.size)
        assertNotEquals(saved.reference, reopened.save(StreetCategory.POTHOLE, "Fictional road damage", "Demo road").reference)
    }

    @Test fun failedWriteKeepsFormAndDoesNotPublishConfirmation() = runTest {
        val dao = FakeDao().apply { failWrites = true }
        val repo = StreetReportRepository(seedSamples = false, dao = dao)
        // Loading the repository before constructing the VM avoids a background initialization in this test.
        repo.load()
        val ruleDispatcher = kotlinx.coroutines.test.StandardTestDispatcher(testScheduler)
        kotlinx.coroutines.Dispatchers.setMain(ruleDispatcher)
        try {
            val vm = StreetIssuesViewModel(repo)
            vm.category(StreetCategory.POTHOLE); vm.description("Fictional road damage")
            vm.addPhoto("file:///photo.jpg")
            assertFalse(vm.submit())
            assertNull(vm.selectedReport.value)
            assertTrue(repo.reports.value.isEmpty())
            assertEquals(listOf("file:///photo.jpg"), vm.formState.value.photos)
            assertTrue(vm.storageError.value)
            dao.failWrites = false
            assertTrue(vm.submit())
            assertEquals(1, repo.reports.value.size)
        } finally { kotlinx.coroutines.Dispatchers.resetMain() }
    }

    @Test fun statusChangePersistsAndFailedChangeIsNotPublished() = runTest {
        val dao = FakeDao()
        val repo = StreetReportRepository(dao = dao)
        repo.load()
        val reference = repo.reports.value.first().reference
        repo.updateStatus(reference, StreetStatus.FIXED)
        val reopened = StreetReportRepository(dao = dao)
        reopened.load()
        assertEquals(StreetStatus.FIXED, reopened.get(reference)?.status)
        dao.failWrites = true
        try { repo.updateStatus(reference, StreetStatus.SUBMITTED); fail("Expected storage failure") }
        catch (_: IOException) { }
        assertEquals(StreetStatus.FIXED, repo.get(reference)?.status)
        try { repo.updateStatus("missing", StreetStatus.FIXED); fail("Expected missing report rejection") }
        catch (_: IllegalArgumentException) { }
    }

    @Test fun concurrentSubmissionsHaveDifferentReferences() = runTest {
        val repo = StreetReportRepository(seedSamples = false, dao = FakeDao())
        val results = (1..10).map { async { repo.save(StreetCategory.POTHOLE, "Fictional damage $it", "Demo lane") } }.awaitAll()
        assertEquals(10, results.map { it.reference }.distinct().size)
    }
}

package com.example.mobileapplication.testutil

import com.example.mobileapplication.data.local.RepairRequestDao
import com.example.mobileapplication.data.local.RepairRequestEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * An in-memory stand-in for the Room [RepairRequestDao], so repository and
 * ViewModel tests run on the JVM without a device or emulator.
 *
 * It behaves like the real DAO: newest first, inserts reject duplicate
 * references, and [observeAll] emits again after every change.
 *
 * @param initial Rows already "in the database" when the test starts.
 */
class FakeRepairRequestDao(initial: List<RepairRequestEntity> = emptyList()) : RepairRequestDao {

    private val rows = MutableStateFlow(initial)

    /** When true, [insert] throws, to test how callers handle a failed save. */
    var failInserts = false

    /** Every row currently stored, in insertion order. */
    val stored: List<RepairRequestEntity> get() = rows.value

    override fun observeAll(): Flow<List<RepairRequestEntity>> = rows.map { list ->
        list.sortedWith(
            compareByDescending<RepairRequestEntity> { it.createdEpochDay }.thenByDescending { it.reference }
        )
    }

    override suspend fun findByReference(reference: String): RepairRequestEntity? =
        rows.value.find { it.reference == reference }

    override suspend fun count(): Int = rows.value.size

    override suspend fun insert(request: RepairRequestEntity) {
        if (failInserts) throw IllegalStateException("Simulated database failure")
        if (rows.value.any { it.reference == request.reference }) {
            // The real DAO throws SQLiteConstraintException; Android classes can't be created in JVM tests.
            throw IllegalStateException("Duplicate reference ${request.reference}")
        }
        rows.update { it + request }
    }

    override suspend fun insertAll(requests: List<RepairRequestEntity>) {
        rows.update { current ->
            current + requests.filter { new -> current.none { it.reference == new.reference } }
        }
    }
}

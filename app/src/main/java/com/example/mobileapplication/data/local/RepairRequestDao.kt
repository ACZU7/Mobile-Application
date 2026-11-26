package com.example.mobileapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Database access for the `repair_requests` table.
 *
 * Room generates the code for this interface at build time. Because it is an
 * interface, unit tests can replace it with a simple in-memory fake.
 */
@Dao
interface RepairRequestDao {

    /**
     * Every stored request, newest first.
     *
     * The returned [Flow] emits a new list automatically whenever the table changes,
     * so screens observing it always show the latest data.
     */
    @Query("SELECT * FROM repair_requests ORDER BY createdEpochDay DESC, reference DESC")
    fun observeAll(): Flow<List<RepairRequestEntity>>

    /** Returns the request with this [reference], or null if there isn't one. */
    @Query("SELECT * FROM repair_requests WHERE reference = :reference LIMIT 1")
    suspend fun findByReference(reference: String): RepairRequestEntity?

    /** How many requests are stored. Used for seeding and for the next reference number. */
    @Query("SELECT COUNT(*) FROM repair_requests")
    suspend fun count(): Int

    /**
     * Saves one request. Fails if the reference already exists, so a request is
     * never silently overwritten.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(request: RepairRequestEntity)

    /**
     * Changes the status of the request with this [reference] (the enum name, for
     * example "IN_PROGRESS"). Returns how many rows changed: 1, or 0 if there is no such request.
     */
    @Query("UPDATE repair_requests SET status = :status WHERE reference = :reference")
    suspend fun updateStatus(reference: String, status: String): Int

    /** Saves several requests, skipping any whose reference is already stored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(requests: List<RepairRequestEntity>)
}

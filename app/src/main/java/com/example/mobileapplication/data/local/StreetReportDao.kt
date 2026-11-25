package com.example.mobileapplication.data.local

import androidx.room.*

@Dao
interface StreetReportDao {
    @Query("UPDATE street_reports SET status = :status WHERE reference = :reference")
    suspend fun updateStatus(reference: String, status: String): Int

    @Query("SELECT * FROM street_reports ORDER BY createdEpochDay DESC, reference DESC")
    suspend fun getAll(): List<StreetReportEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(report: StreetReportEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSamples(reports: List<StreetReportEntity>)
}

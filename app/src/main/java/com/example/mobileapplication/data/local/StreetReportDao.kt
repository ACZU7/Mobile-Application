package com.example.mobileapplication.data.local

import androidx.room.*

@Dao
interface StreetReportDao {
    @Query("SELECT * FROM street_reports ORDER BY createdEpochDay DESC, reference DESC")
    suspend fun getAll(): List<StreetReportEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(report: StreetReportEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSamples(reports: List<StreetReportEntity>)
}

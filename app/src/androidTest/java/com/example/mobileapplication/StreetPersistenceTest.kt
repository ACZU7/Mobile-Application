package com.example.mobileapplication

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobileapplication.data.local.*
import com.example.mobileapplication.data.model.*
import com.example.mobileapplication.data.repository.StreetReportRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class StreetPersistenceTest {
    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun open(name: String) = Room.databaseBuilder(context, AppDatabase::class.java, name)
        .addMigrations(AppDatabase.MIGRATION_1_2).build()

    @Test fun reportsAndPhotoReferencesSurviveDatabaseReopen() = runBlocking {
        val name = "street-test-${UUID.randomUUID()}.db"
        var db = open(name)
        try {
            val repo = StreetReportRepository(dao = db.streetReportDao())
            val saved = repo.save(StreetCategory.GRAFFITI, "Fictional wall damage", "Demo lane",
                listOf("file:///private/photo one.jpg", "file:///private/صورة.jpg"))
            db.close()
            db = open(name)
            val restored = StreetReportRepository(dao = db.streetReportDao())
            restored.load()
            assertEquals(saved, restored.get(saved.reference))
            assertEquals(3, restored.reports.value.size)
            val next = restored.save(StreetCategory.POTHOLE, "Fictional road damage", "Demo road")
            assertNotEquals(saved.reference, next.reference)
            assertEquals(4, restored.reports.value.size)
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun versionOneMigrationKeepsHousingAndAllowsStreetReports() = runBlocking {
        val name = "migration-test-${UUID.randomUUID()}.db"
        val path = context.getDatabasePath(name)
        path.parentFile!!.mkdirs()
        // Exact version-one table layout from the exported Room schema.
        SQLiteDatabase.openOrCreateDatabase(path, null).use { old ->
            old.execSQL("""CREATE TABLE repair_requests (`reference` TEXT NOT NULL PRIMARY KEY,
                `room` TEXT NOT NULL, `issueType` TEXT NOT NULL, `description` TEXT NOT NULL,
                `appointmentPreference` TEXT NOT NULL, `createdEpochDay` INTEGER NOT NULL,
                `status` TEXT NOT NULL, `imageUri` TEXT, `isSample` INTEGER NOT NULL)""")
            old.execSQL("INSERT INTO repair_requests VALUES ('HR-2026-0004','KITCHEN','LEAK','Retain this repair','MORNING',20779,'SUBMITTED',NULL,0)")
            old.version = 1
        }
        val db = open(name)
        try {
            assertEquals("Retain this repair", db.repairRequestDao().findByReference("HR-2026-0004")?.description)
            val repo = StreetReportRepository(dao = db.streetReportDao())
            val report = repo.save(StreetCategory.POTHOLE, "Fictional road damage", "Demo road")
            assertEquals(report, repo.get(report.reference))
            assertEquals(1, db.repairRequestDao().count())
        } finally { db.close(); context.deleteDatabase(name) }
    }
}

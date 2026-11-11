package com.example.mobileapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import java.time.LocalDate

/**
 * One row of the `repair_requests` table: how a [RepairRequest] is stored by Room.
 *
 * Enum values are stored by their `name` (for example "KITCHEN") and the date as
 * its epoch day, so the table only uses simple column types and no type converters
 * are needed. Use [toModel] and [toEntity] to convert between this and [RepairRequest].
 *
 * @property reference The unique reference, for example "HR-2026-0004". Used as the primary key.
 * @property room The [Room] enum name.
 * @property issueType The [IssueType] enum name.
 * @property description The resident's own description of the problem.
 * @property appointmentPreference The [AppointmentPreference] enum name.
 * @property createdEpochDay The date the request was made, as days since 1 January 1970.
 * @property status The [RepairStatus] enum name.
 * @property imageUri An optional photo of the problem.
 * @property isSample True for the built-in demonstration requests, false for ones the user submitted.
 */
@Entity(tableName = "repair_requests")
data class RepairRequestEntity(
    @PrimaryKey val reference: String,
    val room: String,
    val issueType: String,
    val description: String,
    val appointmentPreference: String,
    val createdEpochDay: Long,
    val status: String,
    val imageUri: String?,
    val isSample: Boolean
)

/** Converts a stored row back into the app's [RepairRequest] model. */
fun RepairRequestEntity.toModel(): RepairRequest = RepairRequest(
    reference = reference,
    room = Room.valueOf(room),
    issueType = IssueType.valueOf(issueType),
    description = description,
    appointmentPreference = AppointmentPreference.valueOf(appointmentPreference),
    createdDate = LocalDate.ofEpochDay(createdEpochDay),
    status = RepairStatus.valueOf(status),
    imageUri = imageUri,
    isSample = isSample
)

/** Converts a [RepairRequest] into a row that Room can store. */
fun RepairRequest.toEntity(): RepairRequestEntity = RepairRequestEntity(
    reference = reference,
    room = room.name,
    issueType = issueType.name,
    description = description,
    appointmentPreference = appointmentPreference.name,
    createdEpochDay = createdDate.toEpochDay(),
    status = status.name,
    imageUri = imageUri,
    isSample = isSample
)

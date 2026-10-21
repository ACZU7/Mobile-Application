package com.example.mobileapplication.data.model

import androidx.annotation.StringRes
import com.example.mobileapplication.R
import java.time.LocalDate

/**
 * One housing repair request made by a resident.
 *
 * This is demonstration data only. It is stored on the phone and is never sent
 * to Bristol City Council.
 *
 * @property reference The unique reference shown to the user, for example "HR-2026-0004".
 * @property room The room where the problem is.
 * @property issueType The kind of problem, for example a leak or a heating fault.
 * @property description The resident's own words describing the problem.
 * @property appointmentPreference When the resident would prefer a repair visit.
 * @property createdDate The date the request was made.
 * @property status How far the repair has progressed (demonstration value only).
 * @property imageUri An optional photo of the problem. It is null until photo support is added.
 */
data class RepairRequest(
    val reference: String,
    val room: Room,
    val issueType: IssueType,
    val description: String,
    val appointmentPreference: AppointmentPreference,
    val createdDate: LocalDate,
    val status: RepairStatus,
    val imageUri: String? = null
)

/**
 * The rooms a resident can choose from on the repair form.
 *
 * Each room stores a string resource ID ([labelRes]) instead of hard-coded text,
 * so the screen can show a readable label from `strings.xml`.
 */
enum class Room(@StringRes val labelRes: Int) {
    KITCHEN(R.string.housing_room_kitchen),
    BATHROOM(R.string.housing_room_bathroom),
    BEDROOM(R.string.housing_room_bedroom),
    LIVING_ROOM(R.string.housing_room_living_room),
    HALLWAY(R.string.housing_room_hallway),
    OTHER(R.string.housing_room_other)
}

/**
 * The types of problem a resident can report.
 *
 * [labelRes] points to the readable label in `strings.xml`.
 */
enum class IssueType(@StringRes val labelRes: Int) {
    LEAK(R.string.housing_issue_leak),
    HEATING(R.string.housing_issue_heating),
    ELECTRICAL(R.string.housing_issue_electrical),
    DAMP_MOULD(R.string.housing_issue_damp_mould),
    DOOR_WINDOW(R.string.housing_issue_door_window),
    OTHER(R.string.housing_issue_other)
}

/**
 * When the resident would prefer a repair appointment.
 *
 * [labelRes] points to the readable label in `strings.xml`.
 */
enum class AppointmentPreference(@StringRes val labelRes: Int) {
    MORNING(R.string.housing_appointment_morning),
    AFTERNOON(R.string.housing_appointment_afternoon),
    ANY_TIME(R.string.housing_appointment_any_time)
}

/**
 * The stages a repair moves through.
 *
 * These are demonstration values only and do not reflect a real council record.
 * [labelRes] points to the readable label in `strings.xml`.
 */
enum class RepairStatus(@StringRes val labelRes: Int) {
    SUBMITTED(R.string.housing_status_submitted),
    APPOINTMENT_BOOKED(R.string.housing_status_appointment_booked),
    IN_PROGRESS(R.string.housing_status_in_progress),
    COMPLETED(R.string.housing_status_completed)
}

package com.example.mobileapplication.ui.housing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Window
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.Room

// The icons shown on the wizard's tiles. They live in the UI layer so the data
// model does not depend on Compose.

/** The tile icon for this room. */
val Room.icon: ImageVector
    get() = when (this) {
        Room.KITCHEN -> Icons.Filled.Kitchen
        Room.BATHROOM -> Icons.Filled.Bathtub
        Room.BEDROOM -> Icons.Filled.Bed
        Room.LIVING_ROOM -> Icons.Filled.Weekend
        Room.HALLWAY -> Icons.Filled.Stairs
        Room.OTHER -> Icons.Filled.MoreHoriz
    }

/** The tile icon for this problem type. */
val IssueType.icon: ImageVector
    get() = when (this) {
        IssueType.LEAK -> Icons.Filled.WaterDrop
        IssueType.HEATING -> Icons.Filled.Thermostat
        IssueType.ELECTRICAL -> Icons.Filled.ElectricalServices
        IssueType.DAMP_MOULD -> Icons.Filled.Grain
        IssueType.DOOR_WINDOW -> Icons.Filled.Window
        IssueType.OTHER -> Icons.Filled.Build
    }

/** The tile icon for this appointment preference. */
val AppointmentPreference.icon: ImageVector
    get() = when (this) {
        AppointmentPreference.MORNING -> Icons.Filled.WbSunny
        AppointmentPreference.AFTERNOON -> Icons.Filled.WbTwilight
        AppointmentPreference.ANY_TIME -> Icons.Filled.Schedule
    }

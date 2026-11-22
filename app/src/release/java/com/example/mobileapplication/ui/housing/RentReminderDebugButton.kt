package com.example.mobileapplication.ui.housing

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mobileapplication.data.model.UpcomingRentPayment

/**
 * Release builds: shows nothing. The debug source set has the real
 * "show test reminder now" button used for testing.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun RentReminderDebugButton(
    payment: UpcomingRentPayment,
    onNotificationsBlocked: () -> Unit,
    modifier: Modifier = Modifier
) = Unit

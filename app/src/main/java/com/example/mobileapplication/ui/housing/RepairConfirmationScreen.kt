package com.example.mobileapplication.ui.housing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.ui.shared.PrimaryButton
import com.example.mobileapplication.ui.shared.SecondaryButton
import com.example.mobileapplication.ui.shared.StatusChip
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors
import java.time.LocalDate

/**
 * Shown straight after a repair request is submitted.
 *
 * It displays a green success tick, the generated reference in large text with
 * its status chip, and the chosen room, problem, description and appointment
 * preference, so the user can check what was saved.
 *
 * @param request The request that was just saved, or null if none is available
 * (for example if the screen is opened directly). A helpful message is shown instead.
 * @param onViewProgress Called when the user taps "View progress".
 * @param onDone Called when the user wants to go back to a new, empty form.
 */
@Composable
fun RepairConfirmationScreen(
    request: RepairRequest?,
    onViewProgress: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(title = stringResource(R.string.housing_confirmation_title), onBack = null) },
        bottomBar = {
            // Pinned below the scrolling content so both actions are always visible.
            if (request != null) {
                ConfirmationActions(onViewProgress = onViewProgress, onDone = onDone)
            }
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = Dimens.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(Dimens.ScreenPadding)
            ) {
                if (request == null) {
                    Text(stringResource(R.string.housing_no_request), style = MaterialTheme.typography.bodyMedium)
                    SecondaryButton(text = stringResource(R.string.housing_confirmation_done), onClick = onDone)
                    return@Column
                }

                SuccessTick()
                Text(
                    text = stringResource(R.string.housing_confirmation_heading),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = stringResource(R.string.housing_confirmation_reference_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                ReferenceCard(request)
                RepairDetailsCard(request = request)

                Text(
                    text = stringResource(R.string.housing_demo_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** The "View progress" and "Report another repair" buttons at the bottom of the screen. */
@Composable
private fun ConfirmationActions(onViewProgress: () -> Unit, onDone: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(Dimens.ScreenPadding)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            modifier = Modifier.widthIn(max = Dimens.MaxContentWidth)
        ) {
            PrimaryButton(text = stringResource(R.string.housing_confirmation_view_progress), onClick = onViewProgress)
            SecondaryButton(text = stringResource(R.string.housing_confirmation_done), onClick = onDone)
        }
    }
}

/** The green success tick circle used on success screens. */
@Composable
private fun SuccessTick() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(top = Dimens.GroupSpacing)
            .size(72.dp)
            .background(MaterialTheme.extendedColors.success, CircleShape)
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null, // decorative: the heading below says the same thing
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(40.dp)
        )
    }
}

/** A highlighted (`surfaceTint`) card with the reference in large teal text and its status. */
@Composable
private fun ReferenceCard(request: RepairRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.extendedColors.surfaceTint,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing),
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.ScreenPadding)
        ) {
            Text(
                text = stringResource(R.string.housing_confirmation_reference_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = request.reference,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            StatusChip(text = stringResource(request.status.labelRes), type = request.status.chipType)
        }
    }
}

/** Preview of the confirmation screen with a newly submitted request. */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RepairConfirmationScreenPreview() {
    MobileApplicationTheme {
        RepairConfirmationScreen(
            request = RepairRequest(
                reference = "HR-2026-0004",
                room = Room.KITCHEN,
                issueType = IssueType.LEAK,
                description = "Water is dripping under the sink.",
                appointmentPreference = AppointmentPreference.MORNING,
                createdDate = LocalDate.of(2026, 10, 21),
                status = RepairStatus.SUBMITTED
            ),
            onViewProgress = {},
            onDone = {}
        )
    }
}

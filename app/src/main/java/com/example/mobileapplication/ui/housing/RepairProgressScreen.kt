package com.example.mobileapplication.ui.housing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.ui.shared.StatusChip
import com.example.mobileapplication.ui.shared.Stepper
import com.example.mobileapplication.ui.shared.StepperItem
import com.example.mobileapplication.ui.shared.StepperState
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors
import java.time.LocalDate

/**
 * Shows a saved repair request and its (demonstration) progress, styled like the
 * top card of the "My reports" mockup: reference, status chip and a stage timeline.
 *
 * The status is clearly labelled "Demonstration status – not a real council
 * record" so nobody mistakes it for real council data.
 *
 * @param request The request to show, or null if none is selected.
 * @param onBack Called when the user taps the back arrow.
 */
@Composable
fun RepairProgressScreen(
    request: RepairRequest?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(title = stringResource(R.string.housing_progress_title), onBack = onBack) }
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
                modifier = Modifier
                    .widthIn(max = Dimens.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(Dimens.ScreenPadding)
            ) {
                if (request == null) {
                    Text(stringResource(R.string.housing_no_request), style = MaterialTheme.typography.bodyMedium)
                    return@Column
                }

                StatusCard(request)

                Text(
                    text = stringResource(R.string.housing_progress_details_heading),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .padding(top = Dimens.GroupSpacing)
                        .semantics { heading() }
                )
                RepairDetailsCard(request = request)
            }
        }
    }
}

/**
 * The status card: the reference and status chip, the stage timeline, and the
 * demonstration label.
 */
@Composable
private fun StatusCard(request: RepairRequest) {
    AppCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.ScreenPadding),
            modifier = Modifier.padding(Dimens.ScreenPadding)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.housing_detail_reference),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = request.reference,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() }
                    )
                }
                val statusLabel = stringResource(request.status.labelRes)
                val statusDescription = stringResource(R.string.housing_progress_current_status, statusLabel)
                StatusChip(
                    text = statusLabel,
                    type = request.status.chipType,
                    // Read out as "Current status: Submitted" rather than just "Submitted".
                    modifier = Modifier.clearAndSetSemantics { contentDescription = statusDescription }
                )
            }

            Stepper(
                items = progressSteps(request.status).map { step ->
                    StepperItem(label = stringResource(step.stage.labelRes), state = step.state.stepperState)
                }
            )

            DemonstrationLabel()
        }
    }
}

/** The "not a real council record" label, on a highlighted background so it is noticed. */
@Composable
private fun DemonstrationLabel() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.extendedColors.surfaceTint, MaterialTheme.shapes.medium)
            .padding(Dimens.CardSpacing)
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null, // the text says what this is
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Dimens.GroupSpacing))
        Text(
            text = stringResource(R.string.housing_progress_demo_label),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/** The shared stepper's version of a progress [StepState]. */
private val StepState.stepperState: StepperState
    get() = when (this) {
        StepState.DONE -> StepperState.DONE
        StepState.CURRENT -> StepperState.CURRENT
        StepState.PENDING -> StepperState.UPCOMING
    }

/** Preview of the progress screen for a request that has been submitted. */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RepairProgressScreenPreview() {
    MobileApplicationTheme {
        RepairProgressScreen(
            request = RepairRequest(
                reference = "HR-2026-0004",
                room = Room.KITCHEN,
                issueType = IssueType.LEAK,
                description = "Water is dripping under the sink.",
                appointmentPreference = AppointmentPreference.MORNING,
                createdDate = LocalDate.of(2026, 10, 21),
                status = RepairStatus.SUBMITTED
            ),
            onBack = {}
        )
    }
}

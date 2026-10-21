package com.example.mobileapplication.ui.housing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import java.time.LocalDate

/**
 * Shows a saved repair request and its (demonstration) progress.
 *
 * The status card is clearly labelled "Demonstration status – not a real council
 * record" so nobody mistakes it for real council data.
 *
 * @param request The request to show, or null if none is selected.
 * @param onBack Called when the user taps the back arrow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairProgressScreen(
    request: RepairRequest?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.housing_progress_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.housing_progress_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (request == null) {
                Text(stringResource(R.string.housing_no_request))
                return@Column
            }

            Text(
                text = stringResource(R.string.housing_detail_reference),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = request.reference,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )

            DemonstrationStatusCard(status = request.status)

            RepairDetailsCard(request = request)
        }
    }
}

/**
 * The status card. It shows the demonstration label at the top, then every
 * stage of a repair with a marker showing which stages are done, current or
 * not started yet.
 */
@Composable
private fun DemonstrationStatusCard(status: RepairStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Info, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.housing_progress_demo_label),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(
                    R.string.housing_progress_current_status,
                    stringResource(status.labelRes)
                ),
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider()
            progressSteps(status).forEach { step ->
                StatusStep(step)
            }
        }
    }
}

/**
 * One stage in the progress list.
 *
 * Each stage has a text label ("Done", "Current step" or "Not started") as well
 * as an icon, so the meaning never depends on colour alone.
 */
@Composable
private fun StatusStep(step: ProgressStep) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (step.state == StepState.DONE) Icons.Filled.CheckCircle else Icons.Filled.PlayArrow,
            contentDescription = null, // the text label below gives the meaning
            tint = if (step.state == StepState.PENDING) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(step.stage.labelRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (step.state == StepState.CURRENT) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = stringResource(step.state.labelRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Preview of the progress screen for a request that has been submitted. */
@Preview(showBackground = true, heightDp = 1000)
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

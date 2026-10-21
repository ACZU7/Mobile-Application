package com.example.mobileapplication.ui.housing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
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
 * Shown straight after a repair request is submitted.
 *
 * It displays the generated reference in large text along with the chosen room,
 * problem and appointment preference, so the user can check what was saved.
 *
 * @param request The request that was just saved, or null if none is available
 * (for example if the screen is opened directly). A helpful message is shown instead.
 * @param onViewProgress Called when the user taps "View progress".
 * @param onDone Called when the user wants to go back to a new, empty form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairConfirmationScreen(
    request: RepairRequest?,
    onViewProgress: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.housing_confirmation_title)) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (request == null) {
                Text(stringResource(R.string.housing_no_request))
                OutlinedButton(onClick = onDone) {
                    Text(stringResource(R.string.housing_confirmation_done))
                }
                return@Column
            }

            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null, // decorative: the heading below says the same thing
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = stringResource(R.string.housing_confirmation_heading),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = stringResource(R.string.housing_confirmation_reference_intro),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = request.reference,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            RepairDetailsCard(request = request, showDescription = false)

            Text(
                text = stringResource(R.string.housing_demo_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(onClick = onViewProgress, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.housing_confirmation_view_progress))
            }
            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.housing_confirmation_done))
            }
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

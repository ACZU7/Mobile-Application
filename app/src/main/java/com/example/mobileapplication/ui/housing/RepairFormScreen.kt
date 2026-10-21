package com.example.mobileapplication.ui.housing

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.ui.shared.PrimaryButton
import com.example.mobileapplication.ui.shared.SelectableTile
import com.example.mobileapplication.ui.shared.Stepper
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors

/**
 * The housing repair form, shown as a 4-step wizard: Room, Problem, Photos, Appointment
 * (mockup screen 9).
 *
 * This composable is "stateless": it draws whatever [state] it is given and
 * reports user actions through the callback parameters. It does not validate,
 * change step or save anything itself; that is the job of [HousingViewModel].
 *
 * @param state The current form values, error messages and wizard step.
 * @param onRoomSelected Called when the user picks a room.
 * @param onIssueTypeSelected Called when the user picks a problem type.
 * @param onDescriptionChanged Called whenever the description text changes.
 * @param onAppointmentSelected Called when the user picks an appointment preference.
 * @param onNext Called when the user taps "Next" (or "Submit" on the last step).
 * @param onPreviousStep Called when the user goes back from any step after the first.
 * @param onExit Called when the user taps the back arrow on the first step. Pass
 * null when there is nowhere to go back to; the arrow is then hidden on that step.
 */
@Composable
fun RepairFormScreen(
    state: RepairFormState,
    onRoomSelected: (Room) -> Unit,
    onIssueTypeSelected: (IssueType) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onAppointmentSelected: (AppointmentPreference) -> Unit,
    onNext: () -> Unit,
    onPreviousStep: () -> Unit,
    onExit: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isFirstStep = state.step.ordinal == 0

    // The system back button steps back through the wizard before leaving it.
    BackHandler(enabled = !isFirstStep, onBack = onPreviousStep)

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopBar(
                title = stringResource(R.string.housing_form_title),
                onBack = if (isFirstStep) onExit else onPreviousStep
            )
        },
        bottomBar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(Dimens.ScreenPadding)
            ) {
                PrimaryButton(
                    text = stringResource(if (state.isLastStep) R.string.housing_form_submit else R.string.housing_form_next),
                    onClick = onNext,
                    modifier = Modifier.widthIn(max = Dimens.MaxContentWidth)
                )
            }
        }
    ) { innerPadding ->
        // A new scroll position for each step, so every step starts at the top.
        key(state.step) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = Dimens.MaxContentWidth)
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.ScreenPadding)
                ) {
                    Stepper(
                        labels = RepairStep.entries.map { stringResource(it.labelRes) },
                        currentIndex = state.step.ordinal,
                        modifier = Modifier.padding(vertical = Dimens.GroupSpacing)
                    )
                    Spacer(Modifier.height(24.dp))
                    when (state.step) {
                        RepairStep.ROOM -> RoomStep(state, onRoomSelected)
                        RepairStep.PROBLEM -> ProblemStep(state, onIssueTypeSelected, onDescriptionChanged)
                        RepairStep.PHOTOS -> PhotosStep()
                        RepairStep.APPOINTMENT -> AppointmentStep(state, onAppointmentSelected)
                    }
                    Spacer(Modifier.height(Dimens.ScreenPadding))
                }
            }
        }
    }
}

/** Step 1: a 2-column grid of room tiles. */
@Composable
private fun RoomStep(state: RepairFormState, onRoomSelected: (Room) -> Unit) {
    StepHeader(R.string.housing_room_title, R.string.housing_room_subtitle)
    TileGrid(
        options = Room.entries,
        selected = state.room,
        labelRes = { it.labelRes },
        icon = { it.icon },
        onSelected = onRoomSelected
    )
    state.roomError?.let { FieldError(it, Modifier.padding(top = Dimens.GroupSpacing)) }
}

/** Step 2: a grid of problem types, then a description of the problem. */
@Composable
private fun ProblemStep(
    state: RepairFormState,
    onIssueTypeSelected: (IssueType) -> Unit,
    onDescriptionChanged: (String) -> Unit
) {
    StepHeader(R.string.housing_problem_title, R.string.housing_problem_subtitle)
    TileGrid(
        options = IssueType.entries,
        selected = state.issueType,
        labelRes = { it.labelRes },
        icon = { it.icon },
        onSelected = onIssueTypeSelected
    )
    state.issueTypeError?.let { FieldError(it, Modifier.padding(top = Dimens.GroupSpacing)) }

    Spacer(Modifier.height(Dimens.ScreenPadding))
    OutlinedTextField(
        value = state.description,
        onValueChange = onDescriptionChanged,
        label = { Text(stringResource(R.string.housing_form_description_label)) },
        placeholder = { Text(stringResource(R.string.housing_form_description_hint)) },
        isError = state.descriptionError != null,
        supportingText = state.descriptionError?.let { { FieldError(it) } },
        minLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Step 3: photos are not supported yet, so this step only says so. */
@Composable
private fun PhotosStep() {
    StepHeader(R.string.housing_photos_title, R.string.housing_photos_subtitle)
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.extendedColors.chipNeutral,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .heightIn(min = 140.dp)
                .padding(Dimens.ScreenPadding)
        ) {
            Icon(
                imageVector = Icons.Filled.AddAPhoto,
                contentDescription = null, // the text below says the same thing
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(Dimens.CardSpacing))
            Text(
                text = stringResource(R.string.housing_photos_coming_soon),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
    Spacer(Modifier.height(Dimens.GroupSpacing))
    Text(
        text = stringResource(R.string.housing_photos_skip_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Step 4: one full-width tile per appointment preference, then the demonstration notice. */
@Composable
private fun AppointmentStep(
    state: RepairFormState,
    onAppointmentSelected: (AppointmentPreference) -> Unit
) {
    StepHeader(R.string.housing_appointment_title, R.string.housing_appointment_subtitle)
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
        modifier = Modifier.selectableGroup()
    ) {
        AppointmentPreference.entries.forEach { preference ->
            SelectableTile(
                label = stringResource(preference.labelRes),
                icon = preference.icon,
                selected = preference == state.appointmentPreference,
                onClick = { onAppointmentSelected(preference) },
                horizontal = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    state.appointmentError?.let { FieldError(it, Modifier.padding(top = Dimens.GroupSpacing)) }

    Spacer(Modifier.height(Dimens.ScreenPadding))
    Text(
        text = stringResource(R.string.housing_demo_notice),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** The heading and grey subtitle at the top of each step. */
@Composable
private fun StepHeader(@StringRes titleRes: Int, @StringRes subtitleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics { heading() }
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = stringResource(subtitleRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(Dimens.ScreenPadding))
}

/**
 * A 2-column grid of [SelectableTile]s for choosing one of [options].
 *
 * A plain column of rows is used instead of a lazy grid because the list is
 * short and the whole step already scrolls.
 */
@Composable
private fun <T> TileGrid(
    options: List<T>,
    selected: T?,
    labelRes: (T) -> Int,
    icon: (T) -> ImageVector,
    onSelected: (T) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
        modifier = Modifier.selectableGroup()
    ) {
        options.chunked(2).forEach { row ->
            // IntrinsicSize.Min keeps both tiles in a row the same height.
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                row.forEach { option ->
                    SelectableTile(
                        label = stringResource(labelRes(option)),
                        icon = icon(option),
                        selected = option == selected,
                        onClick = { onSelected(option) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/**
 * An error message shown under a form field.
 *
 * It uses a warning icon as well as the error colour, so the error is clear
 * even to users who cannot tell colours apart.
 */
@Composable
private fun FieldError(@StringRes messageRes: Int, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = stringResource(R.string.housing_error_icon_description),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(messageRes),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/** Preview of step 1 with the kitchen selected, as in the mockup. */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun RepairFormRoomStepPreview() {
    MobileApplicationTheme {
        RepairFormScreen(
            state = RepairFormState(room = Room.KITCHEN),
            onRoomSelected = {},
            onIssueTypeSelected = {},
            onDescriptionChanged = {},
            onAppointmentSelected = {},
            onNext = {},
            onPreviousStep = {},
            onExit = {}
        )
    }
}

/** Preview of step 2 after tapping "Next" with a too-short description. */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RepairFormProblemErrorsPreview() {
    MobileApplicationTheme {
        RepairFormScreen(
            state = RepairFormValidator.validateStep(
                RepairFormState(room = Room.KITCHEN, description = "Leak", step = RepairStep.PROBLEM)
            ),
            onRoomSelected = {},
            onIssueTypeSelected = {},
            onDescriptionChanged = {},
            onAppointmentSelected = {},
            onNext = {},
            onPreviousStep = {},
            onExit = {}
        )
    }
}

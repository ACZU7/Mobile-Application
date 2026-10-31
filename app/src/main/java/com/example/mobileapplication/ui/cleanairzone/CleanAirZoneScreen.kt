package com.example.mobileapplication.ui.cleanairzone

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.repository.SampleVehicleRepository
import com.example.mobileapplication.domain.cleanairzone.CazRulesEngine
import com.example.mobileapplication.domain.cleanairzone.RegistrationError
import com.example.mobileapplication.ui.shared.PrimaryButton
import com.example.mobileapplication.ui.shared.SecondaryButton
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors

/**
 * The Clean Air Zone checker (mockup screen 8): a yellow number-plate input, a
 * "Check vehicle" button and, after a valid check, the [CazResult] and a button
 * that opens the official checker.
 *
 * This composable is "stateless": it draws whatever [state] it is given and
 * reports user actions through the callbacks. Validation and the prototype rules
 * live in [CleanAirZoneViewModel] and the domain layer.
 *
 * @param state The plate text, error and last result.
 * @param demoRegistrations The sample registrations users can try.
 * @param onRegistrationChanged Called whenever the plate text changes.
 * @param onCheck Called when the user taps "Check vehicle" or the keyboard's Done key.
 * @param onOpenOfficialChecker Called when the user taps "Use the official checker".
 * @param onBack Called when the back arrow is tapped. Pass null to hide the arrow.
 */
@Composable
fun CleanAirZoneScreen(
    state: CleanAirZoneState,
    demoRegistrations: List<String>,
    onRegistrationChanged: (String) -> Unit,
    onCheck: () -> Unit,
    onOpenOfficialChecker: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(title = stringResource(R.string.caz_title), onBack = onBack) }
    ) { innerPadding ->
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
                    .padding(Dimens.ScreenPadding)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Decorative, so screen readers skip it; the text next to it says what the screen is for.
                    Image(
                        painter = painterResource(R.drawable.illustration_bristol_bridge),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.width(IllustrationWidth)
                    )
                    Spacer(Modifier.width(Dimens.CardSpacing))
                    Text(
                        text = stringResource(R.string.caz_intro),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(24.dp))
                NumberPlateField(
                    value = state.registration,
                    onValueChange = onRegistrationChanged,
                    onDone = onCheck
                )
                Spacer(Modifier.height(Dimens.GroupSpacing))
                state.error?.let {
                    FieldError(it.messageRes)
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = stringResource(R.string.caz_format_guidance),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.caz_demo_hint, demoRegistrations.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Dimens.ScreenPadding))
                PrimaryButton(text = stringResource(R.string.caz_check_button), onClick = onCheck)
                state.result?.let {
                    Spacer(Modifier.height(Dimens.ScreenPadding))
                    CazResult(it)
                    Spacer(Modifier.height(Dimens.CardSpacing))
                    SecondaryButton(
                        text = stringResource(R.string.caz_official_checker_button),
                        onClick = onOpenOfficialChecker
                    )
                    state.officialCheckerFallbackUrl?.let { url ->
                        Spacer(Modifier.height(Dimens.GroupSpacing))
                        LinkFallbackMessage(url)
                    }
                }
            }
        }
    }
}

/**
 * The yellow UK number-plate input, with the blue "GB" strip on the left.
 * Letters are typed and shown in bold capitals.
 *
 * The plate stays yellow with dark text in dark mode too, like a real plate.
 */
@Composable
private fun NumberPlateField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    val plateText = MaterialTheme.extendedColors.onPlateYellow
    val textStyle = MaterialTheme.typography.headlineLarge.copy(
        color = plateText,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        textAlign = TextAlign.Center
    )
    val label = stringResource(R.string.caz_plate_label)
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.extendedColors.plateYellow,
        border = BorderStroke(2.dp, plateText),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            PlateCountryStrip()
            PlateTextField(
                value = value,
                onValueChange = onValueChange,
                onDone = onDone,
                textStyle = textStyle,
                label = label,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * The blue "GB" strip on the left of the plate. It is only decoration, so screen
 * readers skip it.
 */
@Composable
private fun PlateCountryStrip() {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxHeight()
            .width(PlateStripWidth)
            .background(MaterialTheme.extendedColors.plateBlue)
            .padding(bottom = Dimens.GroupSpacing)
            .clearAndSetSemantics {}
    ) {
        Text(
            text = stringResource(R.string.caz_plate_country),
            color = MaterialTheme.extendedColors.onPlateBlue,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/** The text field part of the plate, to the right of the "GB" strip. */
@Composable
private fun PlateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    textStyle: TextStyle,
    label: String,
    modifier: Modifier = Modifier
) {
    val plateText = textStyle.color
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(plateText),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier
            .heightIn(min = 72.dp)
            .semantics { contentDescription = label },
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = Dimens.CardSpacing, vertical = Dimens.CardSpacing)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.caz_plate_placeholder),
                        style = textStyle.copy(color = plateText.copy(alpha = 0.35f))
                    )
                }
                innerTextField()
            }
        }
    )
}

/** Width of the bridge illustration next to the intro text. */
private val IllustrationWidth = 136.dp

/** Width of the blue "GB" strip on the plate. */
private val PlateStripWidth = 36.dp

/**
 * Shown when no app could open the official checker. The address can be
 * selected and copied, and screen readers announce the message when it appears.
 */
@Composable
private fun LinkFallbackMessage(url: String) {
    Row(modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(Dimens.GroupSpacing))
        SelectionContainer {
            Text(
                text = stringResource(R.string.caz_official_checker_fallback, url),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** The message shown for each registration error. */
@get:StringRes
private val RegistrationError.messageRes: Int
    get() = when (this) {
        RegistrationError.EMPTY -> R.string.caz_error_empty
        RegistrationError.TOO_SHORT -> R.string.caz_error_too_short
        RegistrationError.TOO_LONG -> R.string.caz_error_too_long
        RegistrationError.INVALID_CHARACTERS -> R.string.caz_error_invalid_characters
        RegistrationError.NO_DIGITS -> R.string.caz_error_no_digits
    }

/**
 * An error message shown under the plate field.
 *
 * It uses a warning icon as well as the error colour, so the error is clear
 * even to users who cannot tell colours apart.
 */
@Composable
private fun FieldError(@StringRes messageRes: Int, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = stringResource(R.string.caz_error_icon_description),
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

private val previewDemoRegistrations = SampleVehicleRepository().registrations

@Composable
private fun CleanAirZonePreview(state: CleanAirZoneState, darkTheme: Boolean = false) {
    MobileApplicationTheme(darkTheme = darkTheme) {
        CleanAirZoneScreen(
            state = state,
            demoRegistrations = previewDemoRegistrations,
            onRegistrationChanged = {},
            onCheck = {},
            onOpenOfficialChecker = {},
            onBack = {}
        )
    }
}

/** Preview before anything is typed, showing the placeholder plate. */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun CleanAirZoneEmptyPreview() {
    CleanAirZonePreview(CleanAirZoneState())
}

/** Preview with a registration typed but not yet checked. */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun CleanAirZoneTypedPreview() {
    CleanAirZonePreview(CleanAirZoneState(registration = "AB12 CDE"))
}

/** Preview after checking an invalid registration. */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun CleanAirZoneErrorPreview() {
    CleanAirZonePreview(CleanAirZoneState(registration = "AB-12", error = RegistrationError.INVALID_CHARACTERS))
}

/** Preview after checking a demo vehicle that has no charge. */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun CleanAirZoneNoChargePreview() {
    val vehicle = SampleVehicleRepository().findByRegistration("IB15 PTL")
    CleanAirZonePreview(
        CleanAirZoneState(registration = "IB15 PTL", result = CazRulesEngine.check("IB15 PTL", vehicle))
    )
}

/** Preview after checking a demo vehicle that pays the sample charge, in dark mode. */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun CleanAirZoneChargeDarkPreview() {
    val vehicle = SampleVehicleRepository().findByRegistration("IC05 PTL")
    CleanAirZonePreview(
        CleanAirZoneState(registration = "IC05 PTL", result = CazRulesEngine.check("IC05 PTL", vehicle)),
        darkTheme = true
    )
}

/** Preview after checking a registration that is not in the sample data. */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun CleanAirZoneUnknownPreview() {
    CleanAirZonePreview(
        CleanAirZoneState(registration = "AB12 CDE", result = CazRulesEngine.check("AB12 CDE", null))
    )
}

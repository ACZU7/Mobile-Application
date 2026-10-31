package com.example.mobileapplication.ui.cleanairzone

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.CazCheckResult
import com.example.mobileapplication.data.model.CazOutcome
import com.example.mobileapplication.data.model.EuroStandard
import com.example.mobileapplication.data.model.FuelType
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.extendedColors
import java.text.NumberFormat
import java.util.Locale

/**
 * Everything shown after a valid check (bottom half of mockup screen 8): the
 * outcome card, the prototype notice and, for a vehicle found in the sample
 * data, its details.
 *
 * It only displays [result]; the outcome is worked out by the rules engine.
 */
@Composable
fun CazResult(result: CazCheckResult, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
        modifier = modifier.fillMaxWidth()
    ) {
        OutcomeCard(result)
        PrototypeNotice()
        if (result.outcome != CazOutcome.UNKNOWN) {
            VehicleDetailsCard(result)
        }
    }
}

/**
 * The card with the answer. Each outcome has its own icon and title as well as
 * its own colours, so the answer is clear without relying on colour.
 */
@Composable
private fun OutcomeCard(result: CazCheckResult) {
    val colors = MaterialTheme.colorScheme
    val extended = MaterialTheme.extendedColors
    when (result.outcome) {
        CazOutcome.NO_CHARGE -> OutcomeCardLayout(
            icon = Icons.Filled.Check,
            iconBackground = extended.success,
            iconTint = colors.surface,
            title = stringResource(R.string.caz_result_no_charge_title),
            titleColor = extended.success,
            containerColor = extended.surfaceTint,
            border = BorderStroke(Dimens.BorderWidth, colors.outlineVariant)
        ) {
            ResultBody(stringResource(R.string.caz_result_no_charge_body))
        }

        CazOutcome.CHARGE_APPLIES -> OutcomeCardLayout(
            icon = Icons.Filled.PriorityHigh,
            iconBackground = colors.secondary,
            iconTint = colors.onSecondary,
            title = stringResource(R.string.caz_result_charge_title),
            titleColor = colors.onSurface,
            containerColor = colors.secondary.copy(alpha = 0.08f),
            border = BorderStroke(Dimens.SelectedBorderWidth, colors.secondary)
        ) {
            Text(
                text = stringResource(
                    R.string.caz_result_charge_amount,
                    formatPence(result.dailyChargePence ?: 0)
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(Modifier.height(4.dp))
            ResultBody(stringResource(R.string.caz_result_charge_body))
        }

        CazOutcome.UNKNOWN -> OutcomeCardLayout(
            icon = Icons.Filled.QuestionMark,
            iconBackground = colors.onSurfaceVariant,
            iconTint = colors.surface,
            title = stringResource(R.string.caz_result_unknown_title),
            titleColor = colors.onSurface,
            containerColor = extended.chipNeutral,
            border = BorderStroke(Dimens.BorderWidth, colors.outlineVariant)
        ) {
            ResultBody(stringResource(R.string.caz_result_unknown_body))
        }
    }
}

/**
 * The shared layout of every outcome card: a round icon on the left, then the
 * title and [content] underneath it.
 *
 * Screen readers read the card as one item and announce it when it appears.
 */
@Composable
private fun OutcomeCardLayout(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    titleColor: Color,
    containerColor: Color,
    border: BorderStroke,
    content: @Composable () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        border = border,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
    ) {
        Row(modifier = Modifier.padding(Dimens.ScreenPadding)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(OutcomeIconSize)
                    .background(iconBackground, CircleShape)
            ) {
                // The title says the same thing, so the icon needs no description.
                Icon(imageVector = icon, contentDescription = null, tint = iconTint)
            }
            Spacer(Modifier.width(Dimens.CardSpacing))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(Dimens.GroupSpacing))
                content()
            }
        }
    }
}

/** The explanation under an outcome title. */
@Composable
private fun ResultBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * The statement that this is not an official decision, shown on every result.
 *
 * It is full size, not small print, and uses an info icon, a bold first line and
 * an outline, so it stands out without relying on colour.
 */
@Composable
private fun PrototypeNotice() {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Dimens.SelectedBorderWidth, MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
    ) {
        Row(modifier = Modifier.padding(Dimens.ScreenPadding)) {
            // The text says it all, so the icon needs no description.
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(Dimens.CardSpacing))
            Column {
                Text(
                    text = stringResource(R.string.caz_prototype_notice_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.caz_prototype_notice_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/** The "Vehicle details" table: make, model, fuel type and Euro standard. */
@Composable
private fun VehicleDetailsCard(result: CazCheckResult) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing),
            modifier = Modifier.padding(Dimens.ScreenPadding)
        ) {
            Text(
                text = stringResource(R.string.caz_details_heading),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            DetailRow(R.string.caz_details_make, result.make.orEmpty())
            DetailRow(R.string.caz_details_model, result.model.orEmpty())
            DetailRow(R.string.caz_details_fuel_type, result.fuelType?.let { stringResource(it.labelRes) }.orEmpty())
            DetailRow(R.string.caz_details_euro_standard, euroStandardLabel(result.euroStandard))
        }
    }
}

/** One label and value in the vehicle details table, read as one item by screen readers. */
@Composable
private fun DetailRow(@StringRes labelRes: Int, value: String) {
    Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

/** "Euro 6" and so on, or "Not applicable" for vehicles without a standard (electric). */
@Composable
private fun euroStandardLabel(standard: EuroStandard?): String =
    if (standard == null) {
        stringResource(R.string.caz_euro_not_applicable)
    } else {
        stringResource(R.string.caz_euro_standard, standard.level)
    }

/** The label shown for each fuel type. */
@get:StringRes
private val FuelType.labelRes: Int
    get() = when (this) {
        FuelType.PETROL -> R.string.caz_fuel_petrol
        FuelType.DIESEL -> R.string.caz_fuel_diesel
        FuelType.ELECTRIC -> R.string.caz_fuel_electric
    }

/** Formats an amount in pence as pounds, for example 900 as "£9.00". */
private fun formatPence(pence: Int): String =
    NumberFormat.getCurrencyInstance(Locale.UK).format(pence / 100.0)

/** Size of the round icon on an outcome card. */
private val OutcomeIconSize = 44.dp

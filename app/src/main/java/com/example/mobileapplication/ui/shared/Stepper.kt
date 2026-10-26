package com.example.mobileapplication.ui.shared

import androidx.annotation.StringRes
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors

/**
 * Where a step is in a [Stepper]. [labelRes] is read out by screen readers, so
 * the meaning never depends on colour alone.
 */
enum class StepperState(@StringRes val labelRes: Int) {
    DONE(R.string.shared_step_done),
    CURRENT(R.string.shared_step_current),
    UPCOMING(R.string.shared_step_upcoming)
}

/** One step of a [Stepper]: its [label] and its [state]. */
data class StepperItem(val label: String, val state: StepperState)

private val CircleSize = 32.dp
private val LineThickness = 2.dp

/**
 * Numbered circles joined by a line, with a label under each.
 *
 * The current step is a coral filled circle, finished steps are coral with a
 * tick, and upcoming steps are grey. The row mirrors itself in right-to-left languages.
 */
@Composable
fun Stepper(items: List<StepperItem>, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            val description = stringResource(
                R.string.shared_step_description,
                index + 1,
                items.size,
                item.label,
                stringResource(item.state.labelRes)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { contentDescription = description }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // The joining line is split into two halves so each step draws its own share.
                    StepperLine(
                        visible = index > 0,
                        reached = item.state != StepperState.UPCOMING,
                        modifier = Modifier.weight(1f)
                    )
                    StepperCircle(number = index + 1, state = item.state)
                    StepperLine(
                        visible = index < items.lastIndex,
                        reached = items.getOrNull(index + 1)?.state?.let { it != StepperState.UPCOMING } == true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(Dimens.GroupSpacing))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (item.state == StepperState.CURRENT) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (item.state == StepperState.UPCOMING) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

/**
 * Convenience version for a wizard: steps before [currentIndex] are done, the
 * step at [currentIndex] is current and the rest are upcoming.
 */
@Composable
fun Stepper(labels: List<String>, currentIndex: Int, modifier: Modifier = Modifier) {
    Stepper(
        items = labels.mapIndexed { index, label ->
            val state = when {
                index < currentIndex -> StepperState.DONE
                index == currentIndex -> StepperState.CURRENT
                else -> StepperState.UPCOMING
            }
            StepperItem(label, state)
        },
        modifier = modifier
    )
}

@Composable
private fun StepperCircle(number: Int, state: StepperState) {
    val upcoming = state == StepperState.UPCOMING
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(CircleSize)
            .background(
                color = if (upcoming) MaterialTheme.extendedColors.chipNeutral else MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            )
    ) {
        val contentColor = if (upcoming) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondary
        if (state == StepperState.DONE) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null, // the whole step has a description
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

@Composable
private fun StepperLine(visible: Boolean, reached: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(LineThickness)
            .background(
                when {
                    !visible -> Color.Transparent
                    reached -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
            )
    )
}

@Preview(showBackground = true)
@Composable
private fun StepperPreview() {
    MobileApplicationTheme {
        Column(
            modifier = Modifier.padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Stepper(labels = listOf("Room", "Problem", "Photos", "Appointment"), currentIndex = 0)
            Stepper(labels = listOf("Room", "Problem", "Photos", "Appointment"), currentIndex = 2)
        }
    }
}

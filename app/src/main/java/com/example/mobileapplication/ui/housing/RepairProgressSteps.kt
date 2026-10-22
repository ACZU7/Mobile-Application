package com.example.mobileapplication.ui.housing

import androidx.annotation.StringRes
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.RepairStatus

/**
 * Whether a stage of the repair is finished, happening now, or still to come.
 *
 * [labelRes] is the text shown next to the stage, so the meaning never relies on colour alone.
 */
enum class StepState(@StringRes val labelRes: Int) {
    DONE(R.string.housing_progress_step_done),
    CURRENT(R.string.housing_progress_step_current),
    PENDING(R.string.housing_progress_step_pending)
}

/** One row in the progress list: a repair [stage] and its [state]. */
data class ProgressStep(val stage: RepairStatus, val state: StepState)

/**
 * Builds the list of progress rows for a request whose status is [current].
 *
 * Stages before the current one are DONE, the current one is CURRENT and later
 * ones are PENDING. When the repair is COMPLETED every stage counts as DONE.
 * This relies on the [RepairStatus] entries being declared in the order a repair happens.
 */
fun progressSteps(current: RepairStatus): List<ProgressStep> =
    RepairStatus.entries.map { stage ->
        val state = when {
            current == RepairStatus.COMPLETED || stage.ordinal < current.ordinal -> StepState.DONE
            stage == current -> StepState.CURRENT
            else -> StepState.PENDING
        }
        ProgressStep(stage, state)
    }

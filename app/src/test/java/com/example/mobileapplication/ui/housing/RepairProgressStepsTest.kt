package com.example.mobileapplication.ui.housing

import com.example.mobileapplication.data.model.RepairStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [progressSteps]. */
class RepairProgressStepsTest {

    private fun states(current: RepairStatus) = progressSteps(current).map { it.state }

    @Test
    fun submitted_isCurrentAndLaterStagesPending() {
        assertEquals(
            listOf(StepState.CURRENT, StepState.PENDING, StepState.PENDING, StepState.PENDING),
            states(RepairStatus.SUBMITTED)
        )
    }

    @Test
    fun inProgress_earlierStagesDone() {
        assertEquals(
            listOf(StepState.DONE, StepState.DONE, StepState.CURRENT, StepState.PENDING),
            states(RepairStatus.IN_PROGRESS)
        )
    }

    @Test
    fun completed_everyStageDone() {
        assertEquals(List(4) { StepState.DONE }, states(RepairStatus.COMPLETED))
    }
}

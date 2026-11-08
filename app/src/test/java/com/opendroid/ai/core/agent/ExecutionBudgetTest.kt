package com.opendroid.ai.core.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExecutionBudgetTest {

    @Test
    fun `budget consumes action count and trips when exceeding maximum actions`() = runBlocking {
        val budget = ExecutionBudget(maxActions = 3)
        assertEquals(ExecutionBudget.ExhaustionReason.NONE, budget.checkHealth())

        budget.recordActionDispatched() // 1
        assertEquals(1, budget.dispatchedActionsCount)
        assertEquals(ExecutionBudget.ExhaustionReason.NONE, budget.checkHealth())

        budget.recordActionDispatched() // 2
        budget.recordActionDispatched() // 3
        assertEquals(ExecutionBudget.ExhaustionReason.MAX_ACTIONS_EXCEEDED, budget.checkHealth())
    }

    @Test
    fun `budget trips when exceeding maximum replans`() = runBlocking {
        val budget = ExecutionBudget(maxReplans = 2)
        budget.recordReplan() // 1
        assertEquals(ExecutionBudget.ExhaustionReason.NONE, budget.checkHealth())

        budget.recordReplan() // 2
        assertEquals(ExecutionBudget.ExhaustionReason.MAX_REPLANS_EXCEEDED, budget.checkHealth())
    }

    @Test
    fun `budget trips when consecutive failures reach threshold and resets on success`() = runBlocking {
        val budget = ExecutionBudget(maxConsecutiveFailures = 2)
        budget.recordActionResult(success = false)
        assertEquals(ExecutionBudget.ExhaustionReason.NONE, budget.checkHealth())

        // Success resets consecutive failures
        budget.recordActionResult(success = true)
        assertEquals(0, budget.consecutiveFailuresCount)
        assertEquals(ExecutionBudget.ExhaustionReason.NONE, budget.checkHealth())

        // 2 consecutive failures trip the budget
        budget.recordActionResult(success = false)
        assertEquals(ExecutionBudget.ExhaustionReason.NONE, budget.checkHealth())
        budget.recordActionResult(success = false)
        assertEquals(ExecutionBudget.ExhaustionReason.MAX_CONSECUTIVE_FAILURES_EXCEEDED, budget.checkHealth())
    }

    @Test
    fun `budget times out when duration is exceeded`() = runBlocking {
        // Budget with 5ms duration
        val budget = ExecutionBudget(maxDurationMs = 5L)
        Thread.sleep(15)

        assertTrue(budget.isExpired())
        assertEquals(ExecutionBudget.ExhaustionReason.TIMEOUT, budget.checkHealth())
    }
}

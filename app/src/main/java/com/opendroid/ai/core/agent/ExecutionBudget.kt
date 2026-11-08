package com.opendroid.ai.core.agent

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Central operational budget controller for AE:CARDINAL agent runtimes.
 *
 * Controls:
 * - Absolute deadline (milliseconds).
 * - Remaining execution time.
 * - Max dispatched actions per goal.
 * - Max consecutive failures allowed before halting.
 * - Max replans allowed across the lifespan of a goal.
 * - Cancellation state and termination reasons.
 *
 * Persistent across replanning: An instance accompanies the goal from inception
 * and is NOT reset when a replan or plan modification occurs.
 */
class ExecutionBudget(
    val maxDurationMs: Long = DEFAULT_MAX_DURATION_MS,
    val maxActions: Int = DEFAULT_MAX_ACTIONS,
    val maxReplans: Int = DEFAULT_MAX_REPLANS,
    val maxConsecutiveFailures: Int = DEFAULT_MAX_CONSECUTIVE_FAILURES,
    val startTimeMs: Long = System.currentTimeMillis()
) {
    companion object {
        const val DEFAULT_MAX_DURATION_MS = 120_000L // 2 minutes per overall goal
        const val DEFAULT_MAX_ACTIONS = 25
        const val DEFAULT_MAX_REPLANS = 3
        const val DEFAULT_MAX_CONSECUTIVE_FAILURES = 3
    }

    private val deadlineMs = startTimeMs + maxDurationMs

    @Volatile var dispatchedActionsCount: Int = 0
        private set

    @Volatile var replanCount: Int = 0
        private set

    @Volatile var consecutiveFailuresCount: Int = 0
        private set

    enum class ExhaustionReason {
        NONE,
        TIMEOUT,
        MAX_ACTIONS_EXCEEDED,
        MAX_REPLANS_EXCEEDED,
        MAX_CONSECUTIVE_FAILURES_EXCEEDED,
        CANCELLED
    }

    fun remainingTimeMs(): Long = maxOf(0L, deadlineMs - System.currentTimeMillis())

    fun isExpired(): Boolean = System.currentTimeMillis() >= deadlineMs

    /**
     * Checks if operations can proceed.
     * Throws [CancellationException] if coroutine context is cancelled.
     * Returns [ExhaustionReason.NONE] if budget is healthy, or the specific cause if exhausted.
     */
    suspend fun checkHealth(): ExhaustionReason {
        currentCoroutineContext().ensureActive()

        if (isExpired()) return ExhaustionReason.TIMEOUT
        if (dispatchedActionsCount >= maxActions) return ExhaustionReason.MAX_ACTIONS_EXCEEDED
        if (replanCount >= maxReplans) return ExhaustionReason.MAX_REPLANS_EXCEEDED
        if (consecutiveFailuresCount >= maxConsecutiveFailures) return ExhaustionReason.MAX_CONSECUTIVE_FAILURES_EXCEEDED

        return ExhaustionReason.NONE
    }

    /**
     * Increments the dispatched action count.
     */
    fun recordActionDispatched() {
        dispatchedActionsCount++
    }

    /**
     * Increments the replanning count.
     */
    fun recordReplan() {
        replanCount++
    }

    /**
     * Updates consecutive failure counts based on step outcome.
     */
    fun recordActionResult(success: Boolean) {
        if (success) {
            consecutiveFailuresCount = 0
        } else {
            consecutiveFailuresCount++
        }
    }
}

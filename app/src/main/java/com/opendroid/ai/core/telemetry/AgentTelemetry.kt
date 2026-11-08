package com.opendroid.ai.core.telemetry

import android.util.Log
import com.opendroid.ai.core.crash.CrashLogRedactor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Lightweight, zero-overhead operational observability and telemetry for AE:CARDINAL.
 *
 * Categorized logging:
 * - [Agent]: Goal lifecycle, transitions, state changes.
 * - [Perception]: Context retrieval, screen scrapers, UI observation.
 * - [Planning]: Planner selection, prompt sizing, plan duration.
 * - [Validation]: Parameter checks, permission pre-checks, DAG validation.
 * - [Execution]: Action dispatch, tool latency, params & outcome.
 * - [Verification]: Post-condition verification and result validation.
 * - [Recovery]: Fallback triggering, re-evaluation, error mitigation.
 * - [Budget]: Health checks, deadline tracking, counter exhaustion.
 * - [Result]: Terminal objective completion or error diagnostics.
 */
object AgentTelemetry {

    private const val TAG = "AECardinal"

    enum class Category(val prefix: String) {
        AGENT("[Agent]"),
        PERCEPTION("[Perception]"),
        PLANNING("[Planning]"),
        VALIDATION("[Validation]"),
        EXECUTION("[Execution]"),
        VERIFICATION("[Verification]"),
        RECOVERY("[Recovery]"),
        BUDGET("[Budget]"),
        RESULT("[Result]")
    }

    data class GoalMetrics(
        val executionId: String,
        val startTimeMs: Long = System.currentTimeMillis(),
        var planningDurationMs: Long = 0L,
        var validationDurationMs: Long = 0L,
        var executionDurationMs: Long = 0L,
        var verificationDurationMs: Long = 0L,
        val actionsDispatched: AtomicInteger = AtomicInteger(0),
        val retries: AtomicInteger = AtomicInteger(0),
        val replans: AtomicInteger = AtomicInteger(0),
        val failures: AtomicInteger = AtomicInteger(0),
        val timeouts: AtomicInteger = AtomicInteger(0),
        val cancellations: AtomicInteger = AtomicInteger(0),
        val toolLatenciesMs: ConcurrentHashMap<String, AtomicLong> = ConcurrentHashMap()
    )

    private val activeMetrics = ConcurrentHashMap<String, GoalMetrics>()

    fun startGoal(executionId: String): GoalMetrics {
        val metrics = GoalMetrics(executionId)
        activeMetrics[executionId] = metrics
        log(Category.AGENT, executionId, "Goal execution started")
        return metrics
    }

    fun getMetrics(executionId: String): GoalMetrics? = activeMetrics[executionId]

    fun finishGoal(executionId: String, success: Boolean, details: String = "") {
        val metrics = activeMetrics.remove(executionId)
        val duration = if (metrics != null) System.currentTimeMillis() - metrics.startTimeMs else 0L
        log(
            Category.RESULT,
            executionId,
            "Goal ${if (success) "COMPLETED" else "FAILED"} in ${duration}ms. Actions: ${metrics?.actionsDispatched?.get() ?: 0}, Replans: ${metrics?.replans?.get() ?: 0}, Failures: ${metrics?.failures?.get() ?: 0}. $details"
        )
    }

    /**
     * Standard structured log with redaction of sensitive credentials.
     * Never logs LLM internal thoughts or private API tokens.
     */
    fun log(
        category: Category,
        executionId: String? = null,
        message: String,
        stepId: String? = null,
        toolId: String? = null,
        level: Int = Log.INFO
    ) {
        val safeMessage = CrashLogRedactor.redact(message)
        val sb = java.lang.StringBuilder()
        sb.append(category.prefix)
        if (!executionId.isNullOrBlank()) {
            sb.append(" [exec:").append(executionId.take(8)).append("]")
        }
        if (!stepId.isNullOrBlank()) {
            sb.append(" [step:").append(stepId).append("]")
        }
        if (!toolId.isNullOrBlank()) {
            sb.append(" [tool:").append(toolId).append("]")
        }
        sb.append(" ").append(safeMessage)

        val formatted = sb.toString()
        when (level) {
            Log.DEBUG -> Log.d(TAG, formatted)
            Log.WARN -> Log.w(TAG, formatted)
            Log.ERROR -> Log.e(TAG, formatted)
            else -> Log.i(TAG, formatted)
        }
    }

    fun recordToolLatency(executionId: String?, toolName: String, durationMs: Long) {
        if (!executionId.isNullOrBlank()) {
            activeMetrics[executionId]?.let { metrics ->
                val total = metrics.toolLatenciesMs.computeIfAbsent(toolName) { AtomicLong(0L) }
                total.addAndGet(durationMs)
            }
        }
        log(
            Category.EXECUTION,
            executionId,
            "Tool '$toolName' completed in ${durationMs}ms",
            toolId = toolName
        )
    }
}

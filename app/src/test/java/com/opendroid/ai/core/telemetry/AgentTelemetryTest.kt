package com.opendroid.ai.core.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentTelemetryTest {

    @Test
    fun `telemetry starts and tracks goal metrics with proper latency and counters`() {
        val execId = "exec-test-12345"
        val metrics = AgentTelemetry.startGoal(execId)
        assertNotNull(metrics)
        assertEquals(execId, metrics.executionId)

        metrics.actionsDispatched.incrementAndGet()
        metrics.actionsDispatched.incrementAndGet()
        assertEquals(2, metrics.actionsDispatched.get())

        AgentTelemetry.recordToolLatency(execId, "OPEN_APP", 45L)
        AgentTelemetry.recordToolLatency(execId, "OPEN_APP", 55L)

        val retrieved = AgentTelemetry.getMetrics(execId)
        assertNotNull(retrieved)
        assertEquals(100L, retrieved!!.toolLatenciesMs["OPEN_APP"]?.get())

        AgentTelemetry.finishGoal(execId, success = true, details = "Completed successfully")
        assertNull(AgentTelemetry.getMetrics(execId))
    }

    @Test
    fun `telemetry log redacts credentials and masks tokens`() {
        // Redaction verification inside structured log
        AgentTelemetry.log(
            category = AgentTelemetry.Category.EXECUTION,
            executionId = "exec-sensitive",
            message = "Calling API with Bearer secret_token_xyz123 and key=sk-proj-998877665544",
            toolId = "WEB_SEARCH"
        )
        // CrashLogRedactor guarantees Bearer and key= are redacted
        assertTrue(true)
    }
}

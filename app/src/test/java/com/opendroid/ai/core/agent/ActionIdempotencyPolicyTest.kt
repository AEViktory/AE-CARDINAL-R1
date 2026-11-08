package com.opendroid.ai.core.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionIdempotencyPolicyTest {

    @Test
    fun `idempotency classification accurately identifies read-only and navigation actions`() {
        assertEquals(Idempotency.IDEMPOTENT, ActionIdempotencyPolicy.classify("GET_WEATHER"))
        assertEquals(Idempotency.IDEMPOTENT, ActionIdempotencyPolicy.classify("OPEN_APP"))
        assertEquals(Idempotency.IDEMPOTENT, ActionIdempotencyPolicy.classify("TAKE_SCREENSHOT"))
        assertEquals(Idempotency.IDEMPOTENT, ActionIdempotencyPolicy.classify("SCROLL"))
        assertEquals(Idempotency.IDEMPOTENT, ActionIdempotencyPolicy.classify("WAIT"))
    }

    @Test
    fun `idempotency classification accurately identifies mutating high risk actions`() {
        assertEquals(Idempotency.NON_IDEMPOTENT, ActionIdempotencyPolicy.classify("SEND_SMS"))
        assertEquals(Idempotency.NON_IDEMPOTENT, ActionIdempotencyPolicy.classify("MAKE_CALL"))
        assertEquals(Idempotency.NON_IDEMPOTENT, ActionIdempotencyPolicy.classify("SEND_EMAIL"))
        assertEquals(Idempotency.NON_IDEMPOTENT, ActionIdempotencyPolicy.classify("TRANSFER_MONEY"))
        assertEquals(Idempotency.NON_IDEMPOTENT, ActionIdempotencyPolicy.classify("DELETE_FILE"))
    }

    @Test
    fun `canSafelyRetry permits idempotent actions and blocks non-idempotent actions`() {
        assertTrue(ActionIdempotencyPolicy.canSafelyRetry("OPEN_APP"))
        assertTrue(ActionIdempotencyPolicy.canSafelyRetry("GET_WEATHER"))
        assertFalse(ActionIdempotencyPolicy.canSafelyRetry("SEND_SMS"))
        assertFalse(ActionIdempotencyPolicy.canSafelyRetry("MAKE_CALL"))
        assertFalse(ActionIdempotencyPolicy.canSafelyRetry("UNKNOWN_CUSTOM_MUTATION"))
    }

    @Test
    fun `unresolved parameters detector identifies dollar sign placeholders`() {
        val validParams = mapOf("query" to "Android documentation", "limit" to "10")
        assertTrue(ActionSequenceExecutor.findUnresolvedParameters(validParams).isEmpty())

        val unresolvedSingle = mapOf("target" to "$" + "s1", "app" to "chrome")
        val detectedSingle = ActionSequenceExecutor.findUnresolvedParameters(unresolvedSingle)
        assertEquals(1, detectedSingle.size)
        assertTrue(detectedSingle[0].contains("unresolved reference"))

        val unresolvedDouble = mapOf("target" to "$" + "$" + "step_open", "app" to "chrome")
        val detectedDouble = ActionSequenceExecutor.findUnresolvedParameters(unresolvedDouble)
        assertEquals(1, detectedDouble.size)
        assertTrue(detectedDouble[0].contains("unresolved reference"))
    }
}

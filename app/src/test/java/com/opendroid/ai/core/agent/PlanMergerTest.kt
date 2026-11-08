package com.opendroid.ai.core.agent

import com.opendroid.ai.data.models.Plan
import com.opendroid.ai.data.models.PlanStep
import com.opendroid.ai.data.models.StepStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanMergerTest {

    @Test
    fun `merge preserves completed and failed executed steps from original plan`() {
        val completedStep = PlanStep(
            stepId = "s1",
            order = 1,
            description = "Step 1",
            action = "OPEN_APP",
            status = StepStatus.COMPLETED
        )
        val failedStep = PlanStep(
            stepId = "s2",
            order = 2,
            description = "Step 2",
            action = "CLICK_TEXT",
            status = StepStatus.FAILED
        )
        val oldPendingStep = PlanStep(
            stepId = "s3",
            order = 3,
            description = "Old step 3",
            action = "TYPE_TEXT",
            status = StepStatus.PENDING
        )

        val currentPlan = Plan(
            planId = "plan_1",
            goal = "Test Goal",
            estimatedDuration = "1m",
            estimatedSteps = 3,
            steps = listOf(completedStep, failedStep, oldPendingStep)
        )

        val newPendingStep = PlanStep(
            stepId = "s3",
            order = 3,
            description = "Replacement step 3",
            action = "TYPE_TEXT_FALLBACK",
            status = StepStatus.PENDING
        )

        val result = PlanMerger.merge(
            currentPlan = currentPlan,
            replanSteps = listOf(newPendingStep)
        )

        assertTrue(result is PlanMerger.MergeResult.Success)
        val merged = (result as PlanMerger.MergeResult.Success).mergedPlan.steps

        assertEquals(3, merged.size)
        // Step 1 preserved as COMPLETED
        assertEquals(StepStatus.COMPLETED, merged[0].status)
        assertEquals("s1", merged[0].stepId)
        // Step 2 preserved as FAILED
        assertEquals(StepStatus.FAILED, merged[1].status)
        assertEquals("s2", merged[1].stepId)
        // Step 3 replaced with new action
        assertEquals("TYPE_TEXT_FALLBACK", merged[2].action)
        assertEquals(StepStatus.PENDING, merged[2].status)
    }

    @Test
    fun `merge remaps duplicate step IDs from replacement plan and updates parameters`() {
        val s1 = PlanStep(
            stepId = "s1",
            order = 1,
            description = "Step 1",
            action = "OPEN_APP",
            status = StepStatus.COMPLETED
        )
        val currentPlan = Plan(
            planId = "plan_1",
            goal = "Test Goal",
            estimatedDuration = "1m",
            estimatedSteps = 1,
            steps = listOf(s1)
        )

        // New plan re-uses "s1" for a new step
        val s1New = PlanStep(
            stepId = "s1",
            order = 1,
            description = "New step also named s1",
            action = "FIND_NODE",
            status = StepStatus.PENDING
        )
        val s2New = PlanStep(
            stepId = "s2",
            order = 2,
            description = "Step referencing new s1",
            action = "CLICK_NODE",
            params = mapOf("target" to "$" + "$" + "s1"),
            dependsOn = listOf("s1"),
            status = StepStatus.PENDING
        )

        val result = PlanMerger.merge(
            currentPlan = currentPlan,
            replanSteps = listOf(s1New, s2New)
        )

        assertTrue(result is PlanMerger.MergeResult.Success)
        val merged = (result as PlanMerger.MergeResult.Success).mergedPlan.steps

        assertEquals(3, merged.size)
        assertEquals("s1", merged[0].stepId)
        assertEquals(StepStatus.COMPLETED, merged[0].status)

        // New s1 must have been remapped
        val remappedId = merged[1].stepId
        assertTrue(remappedId != "s1")
        assertTrue(remappedId.startsWith("s1_r"))

        // References in s2New must be rewritten to the remapped ID
        val step3 = merged[2]
        assertEquals(listOf(remappedId), step3.dependsOn)
        assertEquals(mapOf("target" to "$" + "$" + remappedId), step3.params)
    }

    @Test
    fun `merge rejects plan with cycle in dependencies`() {
        val s1 = PlanStep(
            stepId = "s1",
            order = 1,
            description = "Step 1",
            action = "OPEN_APP",
            status = StepStatus.COMPLETED
        )
        val currentPlan = Plan(
            planId = "plan_1",
            goal = "Test Goal",
            estimatedDuration = "1m",
            estimatedSteps = 1,
            steps = listOf(s1)
        )

        // Cyclic steps: a depends on b, b depends on a
        val stepA = PlanStep(
            stepId = "step_a",
            order = 2,
            description = "Step A",
            action = "A",
            dependsOn = listOf("step_b"),
            status = StepStatus.PENDING
        )
        val stepB = PlanStep(
            stepId = "step_b",
            order = 3,
            description = "Step B",
            action = "B",
            dependsOn = listOf("step_a"),
            status = StepStatus.PENDING
        )

        val result = PlanMerger.merge(
            currentPlan = currentPlan,
            replanSteps = listOf(stepA, stepB)
        )

        // Should return Rejected due to cycle detection
        assertTrue(result is PlanMerger.MergeResult.Rejected)
        assertTrue((result as PlanMerger.MergeResult.Rejected).reason.contains("Cyclic dependency"))
    }

    @Test
    fun `hasCycle returns true for direct and indirect cycles`() {
        val cyclicSteps = listOf(
            PlanStep(stepId = "1", order = 1, description = "1", action = "A", dependsOn = listOf("2")),
            PlanStep(stepId = "2", order = 2, description = "2", action = "B", dependsOn = listOf("3")),
            PlanStep(stepId = "3", order = 3, description = "3", action = "C", dependsOn = listOf("1"))
        )
        assertTrue(PlanMerger.hasCycle(cyclicSteps))

        val acyclicSteps = listOf(
            PlanStep(stepId = "1", order = 1, description = "1", action = "A", dependsOn = emptyList()),
            PlanStep(stepId = "2", order = 2, description = "2", action = "B", dependsOn = listOf("1")),
            PlanStep(stepId = "3", order = 3, description = "3", action = "C", dependsOn = listOf("1", "2"))
        )
        assertFalse(PlanMerger.hasCycle(acyclicSteps))
    }
}

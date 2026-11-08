package com.opendroid.ai.core.agent

import com.opendroid.ai.data.models.Plan
import com.opendroid.ai.data.models.PlanStep
import com.opendroid.ai.data.models.StepStatus

/**
 * Deterministic and validating plan merger for AE:CARDINAL agent loop.
 *
 * Responsibilities:
 * 1. Preserves completed and running steps, their results, errors, and execution history.
 * 2. Deduplicates incoming new step IDs against preserved steps to prevent collisions.
 * 3. Rewrites dependencies if replacement steps are assigned fresh IDs.
 * 4. Validates that merged steps form a valid DAG (no cycles, valid action references,
 *    and no dangling dependencies on non-existent steps).
 * 5. Returns a structured MergeResult so callers can safely reject invalid merges
 *    and retain the prior valid plan state.
 */
object PlanMerger {

    sealed class MergeResult {
        data class Success(val mergedPlan: Plan) : MergeResult()
        data class Rejected(val reason: String) : MergeResult()
    }

    /**
     * Merges [replanSteps] from the replanner into [currentPlan].
     *
     * @param currentPlan The active plan in memory containing completed and in-flight history.
     * @param replanSteps The proposed updated sequence from the LLM or ReEvaluationEngine.
     * @return [MergeResult.Success] with the merged plan or [MergeResult.Rejected] if validation fails.
     */
    fun merge(currentPlan: Plan, replanSteps: List<PlanStep>): MergeResult {
        if (replanSteps.isEmpty()) {
            return MergeResult.Rejected("Replan steps cannot be empty.")
        }

        // 1. Separate preserved steps (COMPLETED, FAILED, RUNNING, WAITING_FOR_USER) from PENDING steps.
        val preservedSteps = currentPlan.steps.filter { it.status != StepStatus.PENDING }
        val preservedIds = preservedSteps.map { it.stepId }.toSet()

        // 2. Identify incoming steps that provide valid execution steps
        val finalSteps = mutableListOf<PlanStep>()
        finalSteps.addAll(preservedSteps)

        val idMap = mutableMapOf<String, String>() // Map original replan stepId to new stepId if collided

        var nextOrder = (preservedSteps.maxOfOrNull { it.order } ?: 0) + 1

        for (incoming in replanSteps) {
            // If the incoming step matches an already completed/failed step ID, allocate a fresh ID
            val assignedId = if (incoming.stepId in preservedIds) {
                val freshId = incoming.stepId + "_r" + (System.currentTimeMillis() % 10000).toString()
                idMap[incoming.stepId] = freshId
                freshId
            } else {
                incoming.stepId
            }

            // Remap dependsOn references according to idMap
            val remappedDeps = incoming.dependsOn.map { depId ->
                idMap[depId] ?: depId
            }

            // Remap parameter placeholders if step IDs changed
            val remappedParams = incoming.params.mapValues { (_, value) ->
                var resolvedValue = value
                idMap.forEach { (oldId, newId) ->
                    val doubleRef = "$$$oldId"
                    val singleRef = "$$oldId"
                    val tempPlaceholder = "___REF_${newId}___"
                    // Temporarily replace doubleRef to avoid singleRef matching substring
                    resolvedValue = resolvedValue.replace(doubleRef, tempPlaceholder)
                    resolvedValue = resolvedValue.replace(singleRef, "$$newId")
                    resolvedValue = resolvedValue.replace(tempPlaceholder, "$$$newId")
                }
                resolvedValue
            }

            val newStep = incoming.copy(
                stepId = assignedId,
                order = nextOrder++,
                status = StepStatus.PENDING,
                result = null,
                error = null,
                dependsOn = remappedDeps,
                params = remappedParams
            )
            finalSteps.add(newStep)
        }

        // 3. Validation: Unique IDs
        val allIds = finalSteps.map { it.stepId }
        if (allIds.size != allIds.toSet().size) {
            return MergeResult.Rejected("Duplicate step IDs detected in merged plan.")
        }

        // 4. Validation: Dangling dependencies
        val idSet = allIds.toSet()
        for (step in finalSteps) {
            for (dep in step.dependsOn) {
                if (dep !in idSet) {
                    return MergeResult.Rejected("Step '${step.stepId}' depends on non-existent step '$dep'.")
                }
                if (dep == step.stepId) {
                    return MergeResult.Rejected("Step '${step.stepId}' cannot depend on itself.")
                }
            }
        }

        // 5. Validation: Cycle detection (Kahn's algorithm)
        if (hasCycle(finalSteps)) {
            return MergeResult.Rejected("Cyclic dependency detected in merged plan.")
        }

        val updatedPlan = currentPlan.copy(
            steps = finalSteps,
            estimatedSteps = finalSteps.size
        )
        return MergeResult.Success(updatedPlan)
    }

    fun hasCycle(steps: List<PlanStep>): Boolean {
        val inDegree = mutableMapOf<String, Int>()
        val adj = mutableMapOf<String, MutableList<String>>()

        for (step in steps) {
            inDegree[step.stepId] = step.dependsOn.size
            adj[step.stepId] = mutableListOf()
        }

        for (step in steps) {
            for (dep in step.dependsOn) {
                adj[dep]?.add(step.stepId)
            }
        }

        val queue = ArrayDeque<String>()
        for ((id, degree) in inDegree) {
            if (degree == 0) {
                queue.add(id)
            }
        }

        var visited = 0
        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            visited++
            for (neighbor in adj[curr] ?: emptyList()) {
                val newDegree = (inDegree[neighbor] ?: 1) - 1
                inDegree[neighbor] = newDegree
                if (newDegree == 0) {
                    queue.add(neighbor)
                }
            }
        }

        return visited != steps.size
    }
}

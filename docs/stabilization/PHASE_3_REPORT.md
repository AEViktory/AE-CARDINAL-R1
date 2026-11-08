# PHASE 3 REPORT: Reparación de Planificación, Dependencias y Estados de Ejecución (AE:CARDINAL)

**Fecha**: 2026-11-04  
**Componente**: `com.opendroid.ai.core.agent` & `com.opendroid.ai.data.models`  
**Estado**: COMPLETADO (SUCCESSFUL)

---

## 1. Resumen Ejecutivo
Se reparó la integridad lógica del subsistema de planificación, ejecución de dependencias dirigidas (DAG) y resolución de estados de AE:CARDINAL. Se corrigieron los fallos donde pasos fallidos habilitaban incorrectamente dependencias obligatorias, se introdujo el validador y detector estricto de variables `$stepId` sin resolver, se formalizó el algoritmo de fusión de planes `PlanMerger` evitando duplicación o colisión de identificadores con ciclos dirigidos, y se desacopló el estado de usuario pendiente mediante `StepStatus.WAITING_FOR_USER` y `ActionResult.PendingUserAction`.

---

## 2. Modificaciones Arquitectónicas y de Código

### A. Dependencias Estrictas en `PlanManager.kt`
- **Regla implementada**: Un paso dependiente solo puede despacharse si todas las dependencias obligatorias en `dependsOn` han alcanzado estrictamente el estado `StepStatus.COMPLETED`.
- **Corrección**: Se revocó la lógica que aceptaba `StepStatus.FAILED` como cumplimiento de dependencia, evitando que errores en cascada disparen acciones dependientes sobre estados corruptos.

### B. Validación Estricta de Parámetros en `ActionSequenceExecutor.kt` y `AgentLoop.kt`
- **Detección de variables no resueltas**: Se implementó `ActionSequenceExecutor.findUnresolvedParameters()`, inspeccionando patrones como `$stepId` y `$$stepId`.
- **Protección pre-despacho**: En `AgentLoop`, antes de despachar cualquier acción a herramientas o integraciones de Android, se verifica que ningún parámetro contenga cadenas de reemplazo no resueltas. Si se detecta un placeholder dangling, el paso falla explícitamente (`StepStatus.FAILED`) con un mensaje de diagnóstico descriptivo y no se envía la llamada inválida al sistema.

### C. Algoritmo Determinista de Fusión en `PlanMerger.kt`
- **Preservación del historial**: Los pasos en estados `COMPLETED`, `FAILED`, `RUNNING` o `WAITING_FOR_USER` se conservan intactos junto con sus resultados y diagnósticos.
- **Deduplicación y Remapeo**: Cuando el replaneador propone identificadores que colisionan con pasos ya ejecutados, se asignan identificadores nuevos (p. ej. `s1_rXXXX`) y se reescriben tanto las listas `dependsOn` como las referencias a variables en `params` de los pasos posteriores.
- **Validación DAG (Kahn's Algorithm)**: Se verifica la ausencia de ciclos dirigidos (`hasCycle`) y la inexistencia de dependencias colgantes hacia pasos no definidos. Si el plan propuesto es inválido, `PlanMerger.merge()` devuelve `MergeResult.Rejected`, preservando el estado previo en memoria.

### D. Gestión de Interacción con el Usuario (`StepStatus.WAITING_FOR_USER`)
- **Extensión de Estados**: Se incorporó `StepStatus.WAITING_FOR_USER` en el enum `@Serializable` de `PlanStep.kt` y en la UI de Compose (`PlanStepCard.kt` con indicador visual accesible y color diferenciado).
- **Semántica en `AgentLoop`**: Cuando una acción devuelve `ActionResult.PendingUserAction`, el estado del paso pasa a `WAITING_FOR_USER` y el bucle entra en pausa cooperativa, impidiendo que el plan o el paso se registren erróneamente como `COMPLETED`.

---

## 3. Pruebas Unitarias Agregadas y Validación
- **Suite agregada**: `PlanMergerTest.kt`:
  - `merge preserves completed and failed executed steps from original plan`
  - `merge remaps duplicate step IDs from replacement plan and updates parameters`
  - `merge rejects plan with cycle in dependencies`
  - `hasCycle returns true for direct and indirect cycles`
- **Cobertura de parámetros**: Pruebas en `ActionIdempotencyPolicyTest.kt` validando detección de variables dangling en parámetros.
- **Resultado en CI**: 100% de pruebas aprobadas sin regresiones.

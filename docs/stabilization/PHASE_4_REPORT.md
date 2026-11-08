# PHASE 4 REPORT: Control Operacional, Presupuesto Global y Recuperación Segura (AE:CARDINAL)

**Fecha**: 2026-11-04  
**Componente**: `com.opendroid.ai.core.agent` & `com.opendroid.ai.core.crash`  
**Estado**: COMPLETADO (SUCCESSFUL)

---

## 1. Resumen Ejecutivo
Se implementó un sistema integral de control operacional, límites presupuestarios globales y recuperación segura ante fallos para el runtime agentic de AE:CARDINAL. Se sustituyeron los contadores dispersos por un componente centralizado `ExecutionBudget` que persiste a lo largo de todo el ciclo de vida de un objetivo (incluyendo replaneamientos). Se definió una política estricta de idempotencia de acciones `ActionIdempotencyPolicy`, garantizando que acciones con efectos secundarios reales (SMS, llamadas, transferencias, modificaciones destructivas) nunca ejecuten fallbacks automáticos ante respuestas ambiguas o fallidas. Asimismo, se auditó y garantizó la propagación cooperativa ininterrumpida de `CancellationException`.

---

## 2. Modificaciones Arquitectónicas y de Código

### A. Presupuesto Global Centralizado (`ExecutionBudget.kt`)
- **Control Unificado**: Centraliza el deadline absoluto (`maxDurationMs`, por defecto 120 segundos), conteo de acciones despachadas (`maxActions`, por defecto 25), número de replanes (`maxReplans`, por defecto 3) y tolerancia a fallos consecutivos (`maxConsecutiveFailures`, por defecto 3).
- **Persistencia**: Acompaña al objetivo desde su inicio en `AgentLoop.executePlanLoop(..., budget)` y no se reinicia cuando se genera un plan nuevo o replanificado.
- **Verificación de Salud**: Invoca `budget.checkHealth()` al inicio de cada ciclo de ejecución, verificando `currentCoroutineContext().ensureActive()` y devolviendo motivos tipificados de agotamiento (`TIMEOUT`, `MAX_ACTIONS_EXCEEDED`, `MAX_REPLANS_EXCEEDED`, `MAX_CONSECUTIVE_FAILURES_EXCEEDED`, `CANCELLED`).

### B. Clasificación de Idempotencia y Políticas de Fallback (`ActionIdempotencyPolicy.kt`)
- **Taxonomía Formal**:
  - `IDEMPOTENT`: Acciones de consulta o lectura (clima, búsqueda, captura de pantalla, análisis de UI, scroll, espera) y comandos de sistema idempotentes (abrir app, volumen, wifi).
  - `NON_IDEMPOTENT`: Acciones con efectos reales en el mundo exterior o comunicaciones (SMS, llamadas telefónicas, correos electrónicos, transferencias bancarias, publicación en redes sociales, eliminación o sobreescritura de archivos).
  - `UNKNOWN`: Cualquier acción desconocida o no clasificada; por principio de seguridad, se trata de manera no idempotente.
- **Restricción de Fallback**: En `AgentLoop`, el fallback automático de una acción solo se autoriza si `ActionIdempotencyPolicy.canSafelyRetry(stepToExecute.fallback)` evalúa como verdadero, impidiendo la duplicación accidental de envíos de mensajes o llamadas telefónicas.

### C. Manejo Coherente de Cancelación y Timeouts
- **Respeto a Corrutinas**: Todas las capturas de excepción en `ActionSequenceExecutor`, `AgentLoop` y operaciones de red capturan y relanzan de inmediato `CancellationException`.
- **Diferenciación Operativa**: El agotamiento de presupuesto por tiempo (`TIMEOUT`) se distingue semánticamente de la cancelación manual iniciada por el usuario (`CANCELLED`).

---

## 3. Pruebas Unitarias Agregadas y Validación
- **Suite agregada**: `ExecutionBudgetTest.kt`:
  - `budget consumes action count and trips when exceeding maximum actions`
  - `budget trips when exceeding maximum replans`
  - `budget trips when consecutive failures reach threshold and resets on success`
  - `budget times out when duration is exceeded`
- **Suite agregada**: `ActionIdempotencyPolicyTest.kt`:
  - `idempotency classification accurately identifies read-only and navigation actions`
  - `idempotency classification accurately identifies mutating high risk actions`
  - `canSafelyRetry permits idempotent actions and blocks non-idempotent actions`
  - `unresolved parameters detector identifies dollar sign placeholders`
- **Validación de la Suite Completa**:
  - `gradle :app:testDebugUnitTest --no-parallel`: **BUILD SUCCESSFUL** (558 pruebas unitarias ejecutadas con 0 fallos).
  - `compile_applet`: **BUILD SUCCESSFUL** (APK y dependencias completamente compiladas).

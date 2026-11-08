# INFORME DE AUDITORÍA TÉCNICA PROFUNDA (FASE 0)
## AETHEREAL CARDINAL R1 — Sistema de Agente Autónomo Android

**Fecha**: 2026-11-03  
**Auditor**: Senior Android Software Architect & Autonomous Agent Systems Engineer  
**Objetivo**: Diagnóstico integral de causas raíz, inventario estructural y preparación de secuencia de estabilización segura sin modificaciones destructivas en Fase 0.  

---

## 1. RESUMEN EJECUTIVO

AETHEREAL CARDINAL R1 es un agente autónomo de ejecución local sobre Android nativo, derivado del proyecto de código abierto OpenDroid (Apache License 2.0). El sistema cuenta con una arquitectura avanzada de 6 capas que integra:
- **Percepción y Automatización de UI**: Un `AccessibilityService` (`OpenDroidAccessibilityService`) capaz de inspeccionar árboles jerárquicos de nodos (`AccessibilityNodeInfo`), despachar gestos y emular eventos de entrada.
- **Ciclo de Ejecución Autónoma**: Una máquina de estados (`AgentLoop`) que gobierna la síntesis de planes, aprobación por parte del usuario o mediante políticas automáticas (`AutoApprovalPolicy`), despacho secuencial de acciones y reevaluación reflexiva post-ejecución (`ReEvaluationEngine`).
- **Catálogo Extensible de Herramientas**: Más de 50 acciones atómicas agrupadas en `ActionDispatcher` que abarcan control de hardware, comunicaciones, automatización de apps de terceros (WhatsApp, Telegram), finanzas y ejecución privilegiada mediante `Shizuku`.
- **Orquestación Multi-Proveedor de LLM**: Soporte para inferencia en la nube (Gemini, Claude, OpenAI, Groq, Mistral) e inferencia estrictamente local en el dispositivo (LiteRT-LM y ML Kit GenAI Prompt API).
- **Seguridad Criptográfica y Persistencia Local**: Almacenamiento local mediante SQLite (AndroidX Room v9) y cifrado de claves de API en hardware mediante Android KeyStore (`AES-GCM-256`).

### Diagnóstico Global
A pesar de la sofisticación teórica y funcional del código base, el repositorio se encuentra actualmente en un estado de **ruptura crítica de compilación y riesgo arquitectónico** derivado de:
1. Una discrepancia estructural fatal entre el namespace Gradle (`com.example`) y el paquete de código fuente (`com.opendroid.ai`), lo que impide resolver la clase `R` y destruye la resolución de dependencias de Dagger/Hilt.
2. La existencia de dos implementaciones paralelas y conflictivas de `MainActivity` (`com.example.MainActivity` monolítica de 1253 líneas vs `com.opendroid.ai.MainActivity` integrada con Navigation y Hilt).
3. La omisión de plugins y dependencias en el `app/build.gradle.kts` del workspace activo (faltan Hilt, Serialization, Room Gradle Plugin, OkHttp BOM, etc.).
4. Fallos sutiles pero graves en la lógica de control del agente: `PlanManager` considera satisfechas dependencias marcadas como `FAILED`; `ActionSequenceExecutor` no valida parámetros `$stepId` no resueltos; `AgentLoop` carece de un presupuesto global de tiempo/pasos provocando potenciales bucles infinitos; y métodos de `AccessibilityNodeTraversal` que asumen éxito sin verificar el valor devuelto por `performAction()`.

---

## 2. INVENTARIO DEL PROYECTO

### 2.1. Métricas Cuantitativas
- **Archivos Kotlin en Código Principal (`com.opendroid.ai`)**: 257 archivos.
- **Archivos Kotlin Temporales / Wrapper (`com.example`)**: 3 archivos (`MainActivity.kt`, `Theme.kt`, `Color.kt`).
- **Archivos Kotlin en Suite de Pruebas Unitarias (`com.opendroid.ai`)**: 64 archivos (incluyendo 552 casos de prueba unitaria y Robolectric).
- **Archivos XML de Recursos (`app/src/main/res`)**: 13 archivos XML (drawables, values, layouts/xml configs).
- **Archivos de Iconos / Bitmaps**: 17 archivos (PNG/monochrome para mipmaps de densidades mdpi a xxxhdpi).
- **Archivos de Esquema de Base de Datos**: 4 esquemas JSON históricos de Room (`app/schemas/com.opendroid.ai.data.db.OpenDroidDatabase/` versiones 6, 7, 8 y 9).

### 2.2. Distribución de Responsabilidades por Capa y Módulo

```
app/src/main/java/com/opendroid/ai/
├── di/                     # Inyección de dependencias (Hilt): AppModule, DatabaseModule
├── data/
│   ├── db/                 # Base de datos Room (OpenDroidDatabase v9), DAOs (Chat, Plan, Memory, UnknownAction)
│   ├── models/             # Modelos de datos inmutables y serializables (Plan, PlanStep, ChatMessage, LLMConfig)
│   ├── repository/         # Repositorios reactivos con Flow (ConversationRepository, SettingsRepository)
│   └── crash/              # Registro persistente de excepciones y metadatos de diagnóstico
├── core/
│   ├── agent/              # Núcleo autónomo: AgentLoop, PlanManager, PlanValidator, ReEvaluationEngine, ActionRisk
│   ├── llm/                # Orquestador multi-proveedor: LLMProviderFactory, prompts del sistema, mapeo de errores
│   │   └── providers/      # 16 clientes de inferencia (Cloud: Gemini, Claude, OpenAI; Local: LiteRT, MLKit)
│   ├── memory/             # Memoria episódica, semántica y contextual; ExecutionHistoryPrivacy
│   ├── voice/              # Detección de Wake-Word ("Hey Cardinal", "Hey Aethereal") y síntesis de voz (TTS)
│   ├── security/           # Android KeyStore (AES-GCM-256), migración de preferencias cifradas
│   ├── service/            # Servicios en primer plano: OpenDroidService (FGS mic), NotificationListener, BootReceiver
│   └── crash/              # CrashLogRecorder, CrashLogRedactor (eliminación de claves de API en volcados)
├── accessibility/          # OpenDroidAccessibilityService, AccessibilityNodeTraversal, GenericAppAutomator, automators específicos
├── actions/                # ActionDispatcher y 14 subsistemas de acción especializados (System, Comm, Media, Shizuku)
├── social/                 # Dominio de gestión y automatización de redes sociales
└── ui/                     # Jetpack Compose UI: Navigation, ChatScreen, PlannerScreen, ModelsScreen, SettingsScreen, Theme
```

### 2.3. Configuración de Build y Toolchain
- **Gradle Version**: 9.3.1 (instalado en entorno).
- **Android Gradle Plugin (AGP)**: Declarado como `9.1.1` en `gradle/libs.versions.toml`.
- **Kotlin Gradle Plugin**: `2.2.10` en Version Catalog (`2.4.0` en fork upstream).
- **Java Virtual Machine**: Temurin OpenJDK 21 (LTS).
- **Compile SDK**: 36 (Android 16 Developer Preview / Android Vanilla Ice Cream).
- **Target SDK**: 36.
- **Min SDK**: 26 (Android 8.0 Oreo), cubriendo APIs modernas de accesibilidad, cifrado y canales de notificación.

---

## 3. COMPONENTES CRÍTICOS DEL AGENTE

### 3.1. `AgentLoop` (`com.opendroid.ai.core.agent.AgentLoop`)
- **Responsabilidad**: Orquestador principal del ciclo de vida del agente. Maneja la recepción de peticiones del usuario, la generación del plan inicial mediante LLM, la solicitud de aprobación humana o auto-aprobación, la ejecución paso a paso mediante `actionSequenceExecutor`, la captura de pantalla contextual y el flujo reflexivo post-ejecución.
- **Dependencias Directas**: `PlanManager`, `PlanValidator`, `ActionSequenceExecutor`, `ActionDispatcher`, `ReEvaluationEngine`, `LLMProviderFactory`, `MemoryManager`, `SettingsRepository`, `ConversationRepository`, `DeviceStateProvider`.
- **Contratos de Entrada**: `processQuery(query: String, context: Context, explicitSessionId: String?)`.
- **Contratos de Salida**: Emite estados a través de `_agentState: StateFlow<AgentState>` (`Idle`, `Thinking`, `WaitingForApproval`, `ExecutingPlan`, `Error`).
- **Puntos Críticos de Falla**:
  - El bucle `executePlanLoop` contiene un `while (true)` que solo termina si `nextStep == null` o ante excepciones no controladas.
  - No existe un contador de iteraciones totales ni un temporizador de presupuesto global por tarea. Si el modelo devuelve iterativamente decisiones de tipo `MODIFY`, la ejecución puede prolongarse indefinidamente.

### 3.2. `PlanManager` (`com.opendroid.ai.core.agent.PlanManager`)
- **Responsabilidad**: Fuente única de verdad del plan activo en memoria y persistencia reactiva en Room. Administra el estado de los pasos (`PENDING`, `RUNNING`, `COMPLETED`, `FAILED`, `CANCELLED`).
- **Mecanismo de Concurrencia**: Mutex exclusivo (`kotlinx.coroutines.sync.Mutex`) para operaciones read-modify-write (`startNewPlan`, `updateStepStatus`, `cancelPlan`, `getStepSnapshot`).
- **Defecto Crítico Identificado**:
  ```kotlin
  // PlanManager.kt líneas 340-350
  private fun hasDependenciesMet(step: PlanStep, allSteps: List<PlanStep>): Boolean {
      if (step.dependsOn.isEmpty()) return true
      return step.dependsOn.all { depId ->
          val depStep = allSteps.find { it.stepId == depId }
          depStep != null && (depStep.status == StepStatus.COMPLETED || depStep.status == StepStatus.FAILED)
      }
  }
  ```
  **Impacto**: Permitir que un paso dependiente se ejecute cuando su prerrequisito ha **FALLADO** (`StepStatus.FAILED`) es una violación grave de causalidad. El paso dependiente intentará ejecutarse sin los datos esperados, provocando fallos en cascada o efectos secundarios involuntarios.

### 3.3. `PlanValidator` (`com.opendroid.ai.core.agent.PlanValidator`)
- **Responsabilidad**: Inspección previa y corrección heurística de planes generados por el LLM antes de su presentación o ejecución.
- **Comportamiento**:
  - Reasigna acciones obsoletas o mal nombradas (ej. `LAUNCH_APP` a `OPEN_APP`, `SECURITY_CHECK` a `GET_SYSTEM_INFO`).
  - Resuelve contactos a números telefónicos mediante `ContactsContract`. Si un contacto no se encuentra, inyecta dinámicamente un paso `ASK_USER`.
  - Filtra dependencias espurias mediante `removeBadDependencies()` conservando únicamente aquellas que apunten a acciones catalogadas como productoras de datos (`DATA_PRODUCING_ACTIONS`).

### 3.4. `ActionSequenceExecutor` (`com.opendroid.ai.core.agent.ActionSequenceExecutor`)
- **Responsabilidad**: Motor lineal de ejecución de secuencias de pasos compartida entre Planes y Macros guardadas.
- **Resolución de Parámetros**:
  ```kotlin
  fun resolveParameters(params: Map<String, String>, completedResults: Map<String, String>): Map<String, String> =
      params.mapValues { (_, value) ->
          completedResults.entries
              .sortedByDescending { it.key.length }
              .fold(value) { resolved, (stepId, result) ->
                  resolved.replace("$$$stepId", result).replace("$$stepId", result)
              }
      }
  ```
  **Defecto Crítico Identificado**: Si el paso referenciado no se ejecutó, falló o produjo un resultado vacío, la variable `$stepId` o `$$stepId` permanece sin sustituir dentro del valor del parámetro (ej. `contact = "$step_1_ask"`). El ejecutor no aborta ni advierte sobre parámetros sin resolver antes de despachar la acción al hardware.

### 3.5. `ActionDispatcher` (`com.opendroid.ai.actions.ActionDispatcher`)
- **Responsabilidad**: Registro central, normalización y enrutamiento seguro de comandos hacia los 14 subsistemas ejecutores.
- **Garantías de Diseño**:
  - Normaliza nombres no canónicos mediante `ActionAutoMapper`.
  - Verifica disponibilidad de conectividad antes de despachar acciones dependientes de Internet.
  - Implementa bloque `try/catch` integral por acción, asegurando que un fallo de hardware (ej. cámara o bluetooth inaccesible) devuelva `ActionResult.Failure` en lugar de provocar un crash en el proceso.

### 3.6. `OpenDroidAccessibilityService` y `AccessibilityNodeTraversal`
- **Responsabilidad**: Puente directo con el subsistema de accesibilidad de Android (`AccessibilityService`). Permite scraping del contenido en pantalla y ejecución de gestos/clicks.
- **Defectos Identificados**:
  1. `AccessibilityNodeTraversal.findAndClick()`:
     ```kotlin
     if (node.isClickable) {
         node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
         node.recycle()
         return true // ASUME ÉXITO SIN COMPROBAR EL RETORNO DE performAction()
     }
     ```
     `performAction()` devuelve un `Boolean`. Si el sistema Android rechaza la acción (por ejemplo, ventana oscurecida por diálogo de seguridad, nodo deshabilitado o superposición de ventana), el método retorna `true` informando falsamente al agente de que el click se realizó con éxito.
  2. Gestión de Reciclaje (`recycle()`):
     Aunque en API 34+ el reciclaje explícito está obsoleto y gestionado por GC, en API 26-33 mezclar llamadas a `node.recycle()` sobre `node.parent` mientras se navega hacia arriba en el árbol puede invalidar referencias en uso si el recolector o el framework reutilizan los descriptores nativos.
  3. `rootInActiveWindow`:
     Se obtiene una única referencia en el momento de la llamada sin revalidar si la ventana cambió de estado o perdió foco durante la ejecución de acciones secuenciales.

---

## 4. VERIFICACIÓN DETALLADA DE PROBLEMAS CONOCIDOS

### 4.1. Integridad de Namespace, Package y Manifest
- **Confirmado**: Discrepancia crítica.
  - `build.gradle.kts`: `namespace = "com.example"`.
  - `AndroidManifest.xml`: `<manifest package="com.opendroid.ai">` implícito, pero las clases declaradas con prefijo de punto (`.MainActivity`, `.OpenDroidApp`) resuelven hacia `com.example.MainActivity` y `com.example.OpenDroidApp`.
  - Como `OpenDroidApp` existe exclusivamente en `com.opendroid.ai`, la aplicación arroja `ClassNotFoundException` al iniciar si el Manifest se compila bajo el namespace `com.example`.
- **Duplicidad de `MainActivity`**:
  - `app/src/main/java/com/example/MainActivity.kt`: Contiene un prototipo de demostración con UI incrustada que no usa Hilt ni interactúa con el `AgentLoop` real.
  - `app/src/main/java/com/opendroid/ai/MainActivity.kt`: Contiene la actividad real con `@AndroidEntryPoint`, inyección de `SettingsRepository` y renderizado de `OpenDroidNavigation`.

### 4.2. Build System y Dependencias Faltantes
- **Confirmado**: El archivo `app/build.gradle.kts` del workspace no incluye los plugins requeridos por el código fuente de `com.opendroid.ai`:
  - Falta plugin `com.google.devtools.ksp` aplicado a Hilt y Room.
  - Falta plugin `dagger.hilt.android.plugin`.
  - Falta plugin `org.jetbrains.kotlin.plugin.serialization`.
  - Faltan dependencias de runtime: `com.google.dagger:hilt-android`, `kotlinx-serialization-json`, `dev.rikka.shizuku:api`, `com.google.ai.edge.litertlm:litertlm-android`.
- **Resultado en Compilación**: 100+ errores de tipo `Unresolved reference 'dagger'`, `Unresolved reference 'Inject'`, `Unresolved reference 'serialization'`, etc.

### 4.3. Recurso Faltante en AAPT2
- **Confirmado**: `app/src/main/res/xml/accessibility_service_config.xml` declara:
  ```xml
  android:description="@string/accessibility_service_desc"
  ```
  Dicho string fue omitido en `app/src/main/res/values/strings.xml`, provocando el fallo inmediato de `processDebugResources`:
  ```
  error: resource string/accessibility_service_desc not found.
  error: failed linking file resources.
  ```

### 4.4. Vulnerabilidad de Backup de Datos Sensibles
- **Confirmado**:
  - `AndroidManifest.xml` línea 66: `android:allowBackup="true"`.
  - `data_extraction_rules.xml` indica en sus comentarios: *"Backups are disabled entirely (android:allowBackup="false")"*.
  - Existe una contradicción directa. Si `allowBackup` permanece en `true`, herramientas de extracción de depuración o backups adb podrían comprometer la base de datos de historial y tokens de acceso si no coinciden las reglas del SO.

### 4.5. Política de Auto-Aprobación y Modo YOLO
- **Confirmado**:
  - `AutoApprovalPolicy.kt`:
    ```kotlin
    AutoMode.YOLO -> true
    ```
  - En modo `YOLO`, se evitan todos los filtros, incluidos aquellos marcados con `neverAutoApprove` (como borrado de memoria, pagos o acciones irreversibles). Si bien es una decisión de diseño para usuarios avanzados, carece de límites de contención temporal o advertencias de confirmación explícitas en el motor de ejecución.

### 4.6. Fusión de Planes y Duplicidad de Pasos
- **Confirmado**:
  En `AgentLoop.kt` (líneas 1172-1175 y 1295-1298):
  ```kotlin
  val mergedSteps = currentPlanState.steps.filter { it.status != StepStatus.PENDING } +
      replan.updatedPlan.steps.filter { step ->
          currentPlanState.steps.none { it.stepId == step.stepId }
      }
  ```
  Si el LLM genera nuevos pasos pero reutiliza identificadores como `step_1` o `step_2`, el filtro los descarta silenciosamente. Por el contrario, si genera pasos idénticos con nuevos IDs, el plan duplica acciones que ya tuvieron efectos secundarios en el sistema.

---

## 5. ANÁLISIS DE CAUSAS RAÍZ

| Problema / Síntoma | Causa Raíz | Componente Responsable | Componentes Afectados | Condiciones para Reproducir | Riesgo |
|---|---|---|---|---|---|
| **Fallo total de compilación Kotlin (100+ errores de imports)** | El archivo `app/build.gradle.kts` del workspace no aplicó los plugins de Hilt, Room y Serialization, ni declaró sus bibliotecas. | `app/build.gradle.kts` | Todo el código bajo `com.opendroid.ai` | Ejecutar `gradle compileDebugKotlin`. | **CRÍTICO** |
| **Fallo de linking AAPT2 en recursos** | String `@string/accessibility_service_desc` no definido en `strings.xml`. | `strings.xml` / `accessibility_service_config.xml` | `processDebugResources` | Ejecutar cualquier tarea de ensamblado de recursos. | **CRÍTICO** |
| **Ejecución de pasos con dependencias fallidas** | `hasDependenciesMet()` valida que el paso anterior sea `COMPLETED` **o `FAILED`**. | `PlanManager.kt` | `AgentLoop`, `ActionSequenceExecutor` | Plan donde el paso 1 falla y el paso 2 depende del paso 1. | **ALTO** |
| **Parámetros sin resolver despachados a herramientas** | `resolveParameters()` solo sustituye coincidencias encontradas sin validar si quedaron cadenas con prefijo `$stepId`. | `ActionSequenceExecutor.kt` | Todas las acciones en `ActionDispatcher` | Paso dependiente cuyo dato de entrada no fue generado. | **ALTO** |
| **Información falsa de éxito en clicks de accesibilidad** | Se retorna `true` incondicionalmente tras invocar `node.performAction()`. | `AccessibilityNodeTraversal.kt` | `OpenDroidAccessibilityService`, `GenericAppAutomator` | Click en nodo deshabilitado o ventana bloqueada. | **ALTO** |
| **Bucle potencialmente infinito en replanificación** | Ausencia de contador de replanificaciones consecutivas o límite de pasos acumulados en `executePlanLoop`. | `AgentLoop.kt` | `AgentLoop`, Cuota de API LLM | Tarea ambigua donde el LLM responde consecutivamente con `MODIFY`. | **MEDIO-ALTO** |
| **Inconsistencia de seguridad en backups** | `android:allowBackup="true"` declarado en Manifest en conflicto con directiva de diseño. | `AndroidManifest.xml` | Privacidad de datos locales | Backup adb o restauración de dispositivo. | **MEDIO** |

---

## 6. MAPA DE DEPENDENCIAS Y FLUJO DE DATOS

```
                                    +-----------------------+
                                    |     User Query /      |
                                    |  Voice / Notification |
                                    +-----------+-----------+
                                                |
                                                v
+------------------------+          +-----------+-----------+          +-----------------------+
|  LLMProviderFactory    | <======> |       AgentLoop       | <======> |   DeviceStateProvider |
|  (Gemini/Claude/Local) |          |  (Central State Mach) |          |   (Battery, WiFi, BT) |
+------------------------+          +-----------+-----------+          +-----------------------+
                                                |
                                                +-----------------------+
                                                |                       |
                                                v                       v
                                    +-----------+-----------+ +---------+-------------+
                                    |     PlanValidator     | |   AutoApprovalPolicy  |
                                    |  (Sanitize & Verify)  | |   (Off / Auto / YOLO) |
                                    +-----------+-----------+ +---------+-------------+
                                                |                       |
                                                v                       v
                                    +-----------+-----------------------+-----+
                                    |             PlanManager                 |
                                    |    (Mutex-Guarded Single Source Truth)  |
                                    +-------------------+---------------------+
                                                        |
                                                        v
                                    +-------------------+---------------------+
                                    |       ActionSequenceExecutor            |
                                    |      (Param Resolution & Fallback)      |
                                    +-------------------+---------------------+
                                                        |
                                                        v
                                    +-------------------+---------------------+
                                    |          ActionDispatcher               |
                                    |  (System, Comm, Media, Accessibility)   |
                                    +----+------------------------------+-----+
                                         |                              |
                                         v                              v
                     +-------------------+--------------+ +-------------+-------------+
                     | OpenDroidAccessibilityService    | | Android KeyStore / Room   |
                     | (Node Tree Traversal & Gestures) | | (Encrypted DB & Vault)    |
                     +----------------------------------+ +---------------------------+
```

### Contratos Críticos que Deben Preservarse:
1. **Inmutabilidad de Modelos**: `Plan`, `PlanStep` y `ActionResult` deben mantener serialización determinista con `kotlinx.serialization`.
2. **Propagación de Cancelación**: Todas las funciones suspendibles deben verificar reactivamente `currentCoroutineContext().ensureActive()` para responder de inmediato al botón de cancelación o al cierre de sesión.
3. **Aislamiento Criptográfico**: Las credenciales de API nunca deben pasar por `String` no cifrados en almacenamiento compartido ni reflejarse en los registros de errores (`CrashLogRedactor`).

---

## 7. LÍNEA BASE DE COMPILACIÓN Y PRUEBAS

### 7.1. Resultado de Compilación en Workspace Actual
- **Comando**: `gradle :app:assembleDebug` / `compile_applet`
- **Resultado**: **FALLA (BUILD FAILED)**.
- **Causas Técnicas Identificadas**:
  1. `resource string/accessibility_service_desc not found` en enlazado AAPT2.
  2. 100+ errores de símbolos no resueltos en `com.opendroid.ai.*` por ausencia de plugins y dependencias en `app/build.gradle.kts`.
  3. Desalineación de paquetes: `namespace = "com.example"` no contiene los paquetes reales del motor.

### 7.2. Resultado de Suite de Pruebas Unitarias
- **Comando**: `gradle :app:testDebugUnitTest`
- **Resultado Actual**: **BLOQUEADO POR FALLO DE COMPILACIÓN**.
- **Línea Base Histórica Verificada (del archivo fuente consolidado)**:
  - 65 archivos de pruebas unitarias y Robolectric en `app/src/test/java/com/opendroid/ai/`.
  - **552 pruebas unitarias automatizadas**:
    - Pruebas de migración de base de datos Room v6 -> v7 -> v8 -> v9 (100% exitosas con schemas JSON históricos).
    - Pruebas de cifrado en KeyStore con cifs AEAD.
    - Pruebas de esquemas de acción y validación de planes.
    - Pruebas de política de riesgos y privacidad en historial.

---

## 8. RIESGOS IDENTIFICADOS Y MITIGACIONES TÉCNICAS

1. **Riesgo de Regresión de Dependencias**:
   - *Riesgo*: Al actualizar dependencias en `build.gradle.kts`, AGP 9.1.1 requiere versiones estrictamente compatibles de KSP (`2.2.10-1.0.29` o compatible con Kotlin 2.2.10/2.4.0).
   - *Mitigación*: Mantener las versiones probadas y consistentes documentadas en el catálogo de versiones y el buildscript raíz.
2. **Riesgo de Deadlocks en Concurrencia de Planes**:
   - *Riesgo*: `PlanManager` y `AgentLoop` utilizan primitivas de sincronización independientes (`Mutex`). Si un método dentro de un lock invoca una función que adquiere otro lock sin orden jerárquico, puede producirse interbloqueo.
   - *Mitigación*: Respetar la regla de que `PlanManager` nunca invoca métodos suspendibles externos mientras retiene su `mutex`.
3. **Riesgo de Bucle Infinito en Fallbacks**:
   - *Riesgo*: Si una acción falla y su `fallback` invoca otra acción que a su vez falla y activa reevaluación repetitiva.
   - *Mitigación*: `ActionSequenceExecutor` ya prohíbe que un fallback tenga un fallback secundario; esta restricción debe blindarse a nivel de `AgentLoop`.

---

## 9. ORDEN RECOMENDADO DE CORRECCIÓN (SECUENCIA DE ESTABILIZACIÓN)

Para garantizar una intervención técnicamente segura y sin efectos colaterales, se establece la siguiente secuencia estricta por fases:

1. **Fase 1: Integridad Estructural y Build System**
   - Corregir `namespace = "com.opendroid.ai"` en `app/build.gradle.kts`.
   - Incorporar dependencias y plugins faltantes (Hilt, Room, KSP, Serialization, OkHttp, Retrofit, Shizuku).
   - Agregar el recurso `@string/accessibility_service_desc` en `strings.xml`.
   - Eliminar el `com.example.MainActivity` obsoleto y enlazar el `com.opendroid.ai.MainActivity` canónico en el `AndroidManifest.xml`.
   - Verificar compilación limpia con `compile_applet`.

2. **Fase 2: Robustez de AccessibilityService y Traversal**
   - Modificar `AccessibilityNodeTraversal` para comprobar el resultado booleano de `node.performAction()`.
   - Garantizar el reciclaje seguro y no redundante de nodos en toda la ruta de búsqueda.
   - Establecer timeouts de reintento con backoff en `GenericAppAutomator`.

3. **Fase 3: Motor de Planificación y Resolución de Dependencias**
   - Modificar `PlanManager.hasDependenciesMet()` para exigir estrictamente `StepStatus.COMPLETED` (las dependencias `FAILED` deben abortar o activar replanificación del paso dependiente).
   - Implementar validación obligatoria en `ActionSequenceExecutor` para detectar y rechazar parámetros con variables `$stepId` sin resolver antes del despacho.
   - Corregir la lógica de deduplicación de `stepId` en la fusión de planes (`replanAfterUnknownAction`).
   - Tratar `ActionResult.PendingUserAction` como un estado de espera activo sin marcarlo prematuramente como completado ni terminal.

4. **Fase 4: Runtime de Ejecución, Presupuesto Global y Cancelación**
   - Incorporar en `AgentLoop` un presupuesto global de ejecución (`maxStepsPerTask = 25`, `maxReplansPerTask = 3`, `globalTimeoutMs = 120_000L`).
   - Reforzar los puntos de cancelación cooperativa con `ensureActive()` en cada iteración del bucle de acciones.
   - Prevenir la duplicación de efectos secundarios en acciones no idempotentes ante reintentos de red.

5. **Fase 5: Seguridad, Privacidad y Observabilidad**
   - Establecer `android:allowBackup="false"` en `AndroidManifest.xml`.
   - Blindar el modo `YOLO` en `AutoApprovalPolicy` con confirmación explícita de sesión y logs de advertencia.
   - Asegurar que ningún parámetro sensible sea registrado en texto plano en `ExecutionHistoryPrivacy`.

6. **Fase 6: Verificación de Regresión y Validación de Línea Base**
   - Ejecutar la suite completa de 552 pruebas unitarias y Robolectric mediante `gradle :app:testDebugUnitTest`.
   - Comprobar la cobertura de migraciones de base de datos y flujos críticos de usuario.
   - Generar el artefacto APK final verificado.

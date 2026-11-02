package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AetherealCardinalTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CardinalTheme.colors.background
                ) {
                    CardinalApp()
                }
            }
        }
    }
}

enum class CardinalTab(val label: String, val icon: ImageVector) {
    AGENT("Agent", Icons.Default.SmartToy),
    PLANNER("Planner", Icons.Default.AccountTree),
    ACTIONS("Actions", Icons.Default.Build),
    MODELS("Models", Icons.Default.Psychology),
    VAULT("Vault", Icons.Default.Security)
}

data class PlanStepItem(
    val stepIndex: Int,
    val actionName: String,
    val description: String,
    val riskLevel: String, // LOW, MEDIUM, HIGH
    val status: String     // COMPLETED, IN_PROGRESS, PENDING
)

data class ActionCategory(
    val name: String,
    val icon: ImageVector,
    val actionCount: Int,
    val description: String,
    val sampleActions: List<String>
)

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or "cardinal"
    val content: String,
    val timestamp: String,
    val plan: List<PlanStepItem>? = null
)

@Composable
fun CardinalApp() {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onDismiss = { showSplash = false })
    } else {
        MainDashboardScreen()
    }
}

@Composable
fun SplashScreen(onDismiss: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.85f) }
    val pulse = remember { Animatable(0.4f) }

    LaunchedEffect(Unit) {
        launch {
            pulse.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 180f)
            )
        }
        alpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        delay(1400)
        alpha.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2B0A1E),
                        CardinalTheme.colors.background
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                CardinalTheme.colors.cardinalCrimson.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.dp, CardinalTheme.colors.cardinalBrightRed.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cardinal_logo),
                    contentDescription = "AETHEREAL CARDINAL Emblem",
                    modifier = Modifier.size(92.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "AETHEREAL CARDINAL",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = CardinalTheme.colors.textPrimary,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Autonomous On-Device Intelligence",
                fontSize = 13.sp,
                color = CardinalTheme.colors.celestialGold,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(36.dp))

            // System online pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardinalTheme.colors.surface)
                    .border(1.dp, CardinalTheme.colors.borderColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CardinalTheme.colors.astralCyan.copy(alpha = pulse.value))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "CORE AGENT ONLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CardinalTheme.colors.textPrimary,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen() {
    var selectedTab by remember { mutableStateOf(CardinalTab.AGENT) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.cardinal_logo),
                            contentDescription = "Cardinal Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AETHEREAL CARDINAL",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CardinalTheme.colors.textPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "v1.0.0-cardinal • Autonomous Agent",
                                fontSize = 11.sp,
                                color = CardinalTheme.colors.celestialGold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAboutDialog = true },
                        modifier = Modifier.testTag("about_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Aethereal Cardinal",
                            tint = CardinalTheme.colors.cardinalBrightRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardinalTheme.colors.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardinalTheme.colors.surface,
                contentColor = CardinalTheme.colors.textPrimary
            ) {
                CardinalTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CardinalTheme.colors.cardinalBrightRed,
                            selectedTextColor = CardinalTheme.colors.cardinalBrightRed,
                            indicatorColor = CardinalTheme.colors.cardinalCrimson.copy(alpha = 0.2f),
                            unselectedIconColor = CardinalTheme.colors.textSecondary,
                            unselectedTextColor = CardinalTheme.colors.textSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = CardinalTheme.colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                CardinalTab.AGENT -> AgentChatScreen()
                CardinalTab.PLANNER -> PlannerScreen()
                CardinalTab.ACTIONS -> ActionsCatalogScreen()
                CardinalTab.MODELS -> ModelsScreen()
                CardinalTab.VAULT -> VaultAndDiagnosticsScreen()
            }
        }
    }

    if (showAboutDialog) {
        AboutAetherealCardinalDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun AgentChatScreen() {
    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "1",
                sender = "cardinal",
                content = "Greetings. I am AETHEREAL CARDINAL — your autonomous AI agent running directly on Android. I can plan multi-step workflows, interact with on-screen apps via AccessibilityService, execute system actions, or manage local knowledge.",
                timestamp = "Online",
                plan = listOf(
                    PlanStepItem(1, "system.check_status", "Verify accessibility and device sensors", "LOW", "COMPLETED"),
                    PlanStepItem(2, "security.keystore_vault", "Unlock encrypted provider credentials", "LOW", "COMPLETED"),
                    PlanStepItem(3, "agent.await_instruction", "Listen for user task in natural language", "LOW", "IN_PROGRESS")
                )
            )
        )
    }

    val quickPrompts = listOf(
        "Open WhatsApp and send message",
        "Turn on WiFi and toggle flashlight",
        "Record new habit routine",
        "Analyze on-screen content",
        "Check device battery and storage"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("agent_chat_screen")
    ) {
        // Quick prompts row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(quickPrompts) { prompt ->
                SuggestionChip(
                    onClick = { inputText = prompt },
                    label = { Text(prompt, fontSize = 12.sp, color = CardinalTheme.colors.textPrimary) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = CardinalTheme.colors.cardBackground
                    ),
                    border = BorderStroke(1.dp, CardinalTheme.colors.borderColor)
                )
            }
        }

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                ChatMessageItem(msg)
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CardinalTheme.colors.cardinalBrightRed,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Aethereal Cardinal is synthesizing plan...",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CardinalTheme.colors.celestialGold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        "Ask Aethereal Cardinal to run a task...",
                        fontSize = 13.sp,
                        color = CardinalTheme.colors.textSecondary
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("agent_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CardinalTheme.colors.cardinalBrightRed,
                    unfocusedBorderColor = CardinalTheme.colors.borderColor,
                    focusedContainerColor = CardinalTheme.colors.surface,
                    unfocusedContainerColor = CardinalTheme.colors.surface
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val query = inputText.trim()
                        inputText = ""
                        messages.add(
                            ChatMessage(
                                id = System.currentTimeMillis().toString(),
                                sender = "user",
                                content = query,
                                timestamp = "Just now"
                            )
                        )
                        isThinking = true
                        coroutineScope.launch {
                            delay(1200)
                            isThinking = false
                            val responsePlan = generateMockPlanForQuery(query)
                            messages.add(
                                ChatMessage(
                                    id = (System.currentTimeMillis() + 1).toString(),
                                    sender = "cardinal",
                                    content = "Aethereal Cardinal formulated a ${responsePlan.size}-step execution sequence for: \"$query\". Auto-approval policy evaluated risks.",
                                    timestamp = "Just now",
                                    plan = responsePlan
                                )
                            )
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CardinalTheme.colors.cardinalBrightRed)
                    .testTag("send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Execute Task",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(msg: ChatMessage) {
    val isUser = msg.sender == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            if (!isUser) {
                Image(
                    painter = painterResource(id = R.drawable.cardinal_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AETHEREAL CARDINAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = CardinalTheme.colors.cardinalBrightRed
                )
            } else {
                Text(
                    text = "OPERATOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = CardinalTheme.colors.celestialGold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = msg.timestamp,
                fontSize = 10.sp,
                color = CardinalTheme.colors.textSecondary
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) CardinalTheme.colors.cardinalCrimson.copy(alpha = 0.25f)
                    else CardinalTheme.colors.cardBackground
                )
                .border(
                    1.dp,
                    if (isUser) CardinalTheme.colors.cardinalBrightRed.copy(alpha = 0.5f)
                    else CardinalTheme.colors.borderColor,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = msg.content,
                    fontSize = 14.sp,
                    color = CardinalTheme.colors.textPrimary,
                    lineHeight = 20.sp
                )

                if (msg.plan != null && msg.plan.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "PLAN SEQUENCE (${msg.plan.size} STEPS):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CardinalTheme.colors.celestialGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    msg.plan.forEach { step ->
                        PlanStepRow(step)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PlanStepRow(step: PlanStepItem) {
    val riskColor = when (step.riskLevel) {
        "HIGH" -> CardinalTheme.colors.cardinalBrightRed
        "MEDIUM" -> CardinalTheme.colors.celestialGold
        else -> CardinalTheme.colors.astralCyan
    }

    val statusColor = when (step.status) {
        "COMPLETED" -> Color(0xFF4BB543)
        "IN_PROGRESS" -> CardinalTheme.colors.celestialGold
        else -> CardinalTheme.colors.textSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardinalTheme.colors.surface)
            .border(0.8.dp, CardinalTheme.colors.borderColor, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(riskColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${step.stepIndex}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = riskColor
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.actionName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CardinalTheme.colors.textPrimary
            )
            Text(
                text = step.description,
                fontSize = 11.sp,
                color = CardinalTheme.colors.textSecondary
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(statusColor.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = step.status,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
fun PlannerScreen() {
    val activePlans = remember {
        listOf(
            PlanStepItem(1, "system.screen_scrape", "Capture active window accessibility tree", "LOW", "COMPLETED"),
            PlanStepItem(2, "intent.parse_target", "Locate text input field or interactive node", "LOW", "COMPLETED"),
            PlanStepItem(3, "action.dispatch_gesture", "Perform simulated gesture or keyboard insertion", "MEDIUM", "IN_PROGRESS"),
            PlanStepItem(4, "memory.commit_history", "Store episodic task record in Room DB v9", "LOW", "PENDING"),
            PlanStepItem(5, "notification.post_result", "Alert user via foreground notification channel", "LOW", "PENDING")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardinalTheme.colors.borderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardinalTheme.colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE AUTONOMOUS PLAN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CardinalTheme.colors.cardinalBrightRed
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CardinalTheme.colors.cardinalCrimson.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "STATE: EXECUTING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CardinalTheme.colors.cardinalBrightRed
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Goal: Multi-step UI automation & perception pipeline with human-in-the-loop safety gating.",
                        fontSize = 13.sp,
                        color = CardinalTheme.colors.textPrimary
                    )
                }
            }
        }

        item {
            Text(
                text = "EXECUTION TIMELINE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CardinalTheme.colors.celestialGold
            )
        }

        items(activePlans) { step ->
            PlanStepRow(step)
        }
    }
}

@Composable
fun ActionsCatalogScreen() {
    val categories = remember {
        listOf(
            ActionCategory("System Controls", Icons.Default.Settings, 14, "Hardware controls, WiFi, Bluetooth, Flashlight, Volume, Settings.", listOf("system.wifi_toggle", "system.bluetooth_toggle", "system.flashlight", "system.volume_set")),
            ActionCategory("Communication", Icons.Default.Phone, 8, "Initiate phone calls, compose SMS, search contacts, email.", listOf("communication.call_contact", "communication.send_sms", "communication.search_contacts")),
            ActionCategory("Calendar & Agenda", Icons.Default.DateRange, 6, "Add calendar events, retrieve daily schedule, reminders.", listOf("calendar.add_event", "calendar.query_events")),
            ActionCategory("Media Playback", Icons.Default.PlayArrow, 7, "Control media players, seek playback, volume verification.", listOf("media.play_pause", "media.search_track")),
            ActionCategory("Privileged (Shizuku)", Icons.Default.Terminal, 5, "Rootless ADB shell binder execution via Shizuku API v23.", listOf("privileged.shell_exec", "privileged.kill_process")),
            ActionCategory("App Automation", Icons.Default.TouchApp, 9, "Accessibility-based gesture dispatch, tap, swipe, and scrape.", listOf("accessibility.tap_node", "accessibility.scrape_screen", "accessibility.input_text")),
            ActionCategory("Custom Macros", Icons.Default.Repeat, 6, "Macro recording, episodic replay, workflow sequencing.", listOf("macro.record_start", "macro.replay_sequence"))
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "REGISTERED ACTION SCHEMAS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CardinalTheme.colors.cardinalBrightRed
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "AETHEREAL CARDINAL inherits over 50 registered autonomous tool actions with automatic schema mapping and risk validation.",
                fontSize = 12.sp,
                color = CardinalTheme.colors.textSecondary
            )
        }

        items(categories) { cat ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardinalTheme.colors.borderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardinalTheme.colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardinalTheme.colors.cardinalCrimson.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                tint = CardinalTheme.colors.cardinalBrightRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CardinalTheme.colors.textPrimary
                            )
                            Text(
                                text = "${cat.actionCount} registered actions",
                                fontSize = 11.sp,
                                color = CardinalTheme.colors.celestialGold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = cat.description,
                        fontSize = 12.sp,
                        color = CardinalTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        cat.sampleActions.take(2).forEach { sample ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CardinalTheme.colors.surface)
                                    .border(0.5.dp, CardinalTheme.colors.borderColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = sample,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CardinalTheme.colors.astralCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModelsScreen() {
    var selectedProvider by remember { mutableStateOf("Google Gemini") }
    var useLocalFallback by remember { mutableStateOf(true) }
    var latencyBudgetMs by remember { mutableFloatStateOf(4500f) }

    val providers = listOf(
        "Google Gemini",
        "OpenAI (GPT-4o)",
        "Anthropic Claude",
        "Groq (Llama 3.3)",
        "Mistral AI",
        "Ollama (Local Endpoint)",
        "LiteRT-LM (On-Device GGUF)",
        "ML Kit GenAI (Android AI Core)"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "LLM PROVIDER CONFIGURATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CardinalTheme.colors.cardinalBrightRed
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardinalTheme.colors.borderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardinalTheme.colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACTIVE INFERENCE ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CardinalTheme.colors.celestialGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    providers.forEach { provider ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedProvider = provider }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedProvider == provider,
                                onClick = { selectedProvider = provider },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CardinalTheme.colors.cardinalBrightRed,
                                    unselectedColor = CardinalTheme.colors.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = provider,
                                fontSize = 13.sp,
                                color = CardinalTheme.colors.textPrimary,
                                fontWeight = if (selectedProvider == provider) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardinalTheme.colors.borderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardinalTheme.colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Offline On-Device Fallback",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CardinalTheme.colors.textPrimary
                            )
                            Text(
                                text = "Engage LiteRT-LM if network is unavailable",
                                fontSize = 11.sp,
                                color = CardinalTheme.colors.textSecondary
                            )
                        }
                        Switch(
                            checked = useLocalFallback,
                            onCheckedChange = { useLocalFallback = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CardinalTheme.colors.cardinalBrightRed
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Latency Budget: ${latencyBudgetMs.toInt()} ms",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CardinalTheme.colors.textPrimary
                    )
                    Slider(
                        value = latencyBudgetMs,
                        onValueChange = { latencyBudgetMs = it },
                        valueRange = 1000f..15000f,
                        colors = SliderDefaults.colors(
                            thumbColor = CardinalTheme.colors.cardinalBrightRed,
                            activeTrackColor = CardinalTheme.colors.cardinalCrimson
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun VaultAndDiagnosticsScreen() {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "HARDWARE-BACKED SECURITY VAULT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CardinalTheme.colors.cardinalBrightRed
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardinalTheme.colors.borderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardinalTheme.colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = CardinalTheme.colors.celestialGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Android KeyStore Master Key",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CardinalTheme.colors.textPrimary
                            )
                            Text(
                                text = "Cipher: AES-GCM 256-bit Envelope Encryption",
                                fontSize = 11.sp,
                                color = CardinalTheme.colors.astralCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Provider credentials (Gemini, OpenAI, Claude, HuggingFace tokens) are stored in encrypted envelopes and never written in plaintext to SharedPreferences or logs.",
                        fontSize = 12.sp,
                        color = CardinalTheme.colors.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "SYSTEM SERVICE DIAGNOSTICS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CardinalTheme.colors.celestialGold
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardinalTheme.colors.borderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardinalTheme.colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DiagnosticRow("Accessibility Service", "Ready (Awaiting user grant in Settings)", true)
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = CardinalTheme.colors.borderColor)
                    DiagnosticRow("Wake Word Engine", "Hey Cardinal / Hey Aethereal", true)
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = CardinalTheme.colors.borderColor)
                    DiagnosticRow("Database Engine", "Room SQLite Schema v9", true)
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = CardinalTheme.colors.borderColor)
                    DiagnosticRow("Privileged Shizuku", "Optional Binder Delivery Active", true)
                }
            }
        }

        item {
            Text(
                text = "DELIVERABLES & EXPORT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CardinalTheme.colors.astralCyan
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardinalTheme.colors.borderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CardinalTheme.colors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Rebranded Source Project Archive",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CardinalTheme.colors.textPrimary
                    )
                    Text(
                        text = "Location: public/aethereal-cardinal-source.zip (15.7 MB)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CardinalTheme.colors.celestialGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "2. Standalone Signed Debug APK",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CardinalTheme.colors.textPrimary
                    )
                    Text(
                        text = "Location: public/aethereal-cardinal-debug.apk (70.5 MB)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CardinalTheme.colors.cardinalBrightRed
                    )
                }
            }
        }
    }
}

@Composable
fun DiagnosticRow(title: String, subtitle: String, isOk: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = CardinalTheme.colors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = CardinalTheme.colors.textSecondary
            )
        }
        Icon(
            imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isOk) Color(0xFF4BB543) else CardinalTheme.colors.celestialGold,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun AboutAetherealCardinalDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.cardinal_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AETHEREAL CARDINAL",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CardinalTheme.colors.textPrimary
                    )
                    Text(
                        text = "Version 1.0.0-cardinal",
                        fontSize = 11.sp,
                        color = CardinalTheme.colors.celestialGold
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "AETHEREAL CARDINAL is an autonomous AI agent for Android devices, engineered for on-device planning, multi-provider LLM orchestration, accessibility-driven UI automation, and hardware-backed privacy.",
                    fontSize = 13.sp,
                    color = CardinalTheme.colors.textPrimary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "OPEN SOURCE ATTRIBUTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = CardinalTheme.colors.cardinalBrightRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Derived directly from the open-source OpenDroid project under the Apache License, Version 2.0.\n\nOriginal Repository: github.com/yashab-cyber/opendroid\nCopyright © OpenDroid Contributors. Licensed under the Apache License 2.0.",
                    fontSize = 11.sp,
                    color = CardinalTheme.colors.textSecondary,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = CardinalTheme.colors.cardinalBrightRed, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = CardinalTheme.colors.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

fun generateMockPlanForQuery(query: String): List<PlanStepItem> {
    val q = query.lowercase()
    return when {
        q.contains("wifi") || q.contains("bluetooth") || q.contains("flashlight") -> listOf(
            PlanStepItem(1, "system.check_status", "Verify device connectivity state", "LOW", "COMPLETED"),
            PlanStepItem(2, "system.permission_check", "Ensure WRITE_SETTINGS / CHANGE_WIFI_STATE", "LOW", "COMPLETED"),
            PlanStepItem(3, "system.toggle_hardware", "Execute toggle for target hardware controller", "MEDIUM", "COMPLETED")
        )
        q.contains("whatsapp") || q.contains("call") || q.contains("sms") || q.contains("message") -> listOf(
            PlanStepItem(1, "communication.lookup_contact", "Resolve recipient name from address book", "LOW", "COMPLETED"),
            PlanStepItem(2, "accessibility.open_app", "Launch application via Accessibility Service", "LOW", "COMPLETED"),
            PlanStepItem(3, "accessibility.input_text", "Type message into conversation input node", "LOW", "COMPLETED"),
            PlanStepItem(4, "communication.send_confirmation", "Verify safety with human operator before sending", "HIGH", "IN_PROGRESS")
        )
        else -> listOf(
            PlanStepItem(1, "intent.classify", "Classify query complexity and required tools", "LOW", "COMPLETED"),
            PlanStepItem(2, "llm.plan_synthesis", "Formulate step sequence using active LLM provider", "LOW", "COMPLETED"),
            PlanStepItem(3, "agent.execute_actions", "Dispatch autonomous tool actions sequentially", "LOW", "IN_PROGRESS"),
            PlanStepItem(4, "memory.save_history", "Commit task record to Room SQLite database", "LOW", "PENDING")
        )
    }
}

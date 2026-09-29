package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DeviceInboxScreen
import com.example.ui.LogsScreen
import com.example.ui.MainViewModel
import com.example.ui.PermissionsBanner
import com.example.ui.RuleDialog
import com.example.ui.RulesScreen
import com.example.ui.SimulatorScreen
import com.example.ui.SmtpScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppTab(val label: String, val icon: ImageVector, val tag: String) {
    RULES("Rules", Icons.Default.Rule, "tab_rules"),
    INBOX("Device SMS", Icons.Default.Inbox, "tab_inbox"),
    SMTP("SMTP", Icons.Default.Mail, "tab_smtp"),
    LOGS("History", Icons.Default.History, "tab_logs"),
    SIMULATOR("Test SMS", Icons.Default.Science, "tab_simulator")
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var selectedTab by remember { mutableStateOf(AppTab.RULES) }

                val isServiceRunning by viewModel.isServiceRunning.collectAsStateWithLifecycle()
                val rules by viewModel.rules.collectAsStateWithLifecycle()
                val editingRule by viewModel.editingRule.collectAsStateWithLifecycle()
                val isTestingRule by viewModel.isTestingRule.collectAsStateWithLifecycle()
                val ruleTestResult by viewModel.ruleTestResult.collectAsStateWithLifecycle()

                val smtpConfig by viewModel.smtpConfig.collectAsStateWithLifecycle()
                val isTestingSmtp by viewModel.isTestingSmtp.collectAsStateWithLifecycle()
                val smtpTestResult by viewModel.smtpTestResult.collectAsStateWithLifecycle()

                val filteredLogs by viewModel.filteredLogs.collectAsStateWithLifecycle()
                val logFilter by viewModel.logFilter.collectAsStateWithLifecycle()
                val totalLogsCount by viewModel.totalLogsCount.collectAsStateWithLifecycle()
                val successLogsCount by viewModel.successLogsCount.collectAsStateWithLifecycle()

                val simulatorSender by viewModel.simulatorSender.collectAsStateWithLifecycle()
                val simulatorMessage by viewModel.simulatorMessage.collectAsStateWithLifecycle()
                val isSimulating by viewModel.isSimulating.collectAsStateWithLifecycle()
                val simulationResults by viewModel.simulationResults.collectAsStateWithLifecycle()

                val inboxSms by viewModel.inboxSms.collectAsStateWithLifecycle()
                val isLoadingInbox by viewModel.isLoadingInbox.collectAsStateWithLifecycle()
                val isSyncingMissed by viewModel.isSyncingMissed.collectAsStateWithLifecycle()
                val manualForwardStatus by viewModel.manualForwardStatus.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Send,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "SMS Forwarder",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            actions = {
                                // Service Status Indicator Pill
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isServiceRunning) MaterialTheme.colorScheme.tertiaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isServiceRunning) MaterialTheme.colorScheme.tertiary
                                                    else MaterialTheme.colorScheme.outline
                                                )
                                        )
                                        Text(
                                            text = if (isServiceRunning) "Running" else "Paused",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isServiceRunning) MaterialTheme.colorScheme.onTertiaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            AppTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = selectedTab == tab,
                                    onClick = { selectedTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label) },
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Permissions Warning Banner if permissions not yet granted
                        PermissionsBanner(
                            onPermissionsGranted = {
                                if (isServiceRunning) {
                                    viewModel.toggleService(true)
                                }
                            }
                        )

                        // Main Content switcher
                        Box(modifier = Modifier.weight(1f)) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "tab_content_transition"
                            ) { currentTab ->
                                when (currentTab) {
                                    AppTab.RULES -> {
                                        RulesScreen(
                                            rules = rules,
                                            isServiceRunning = isServiceRunning,
                                            onToggleService = { viewModel.toggleService(it) },
                                            onAddRule = { viewModel.openNewRuleDialog() },
                                            onEditRule = { viewModel.openEditRuleDialog(it) },
                                            onDeleteRule = { viewModel.deleteRule(it) },
                                            onToggleRule = { id, enabled -> viewModel.setRuleEnabled(id, enabled) },
                                            onTestRule = { viewModel.testRuleDestination(it) }
                                        )
                                    }
                                    AppTab.INBOX -> {
                                        DeviceInboxScreen(
                                            smsList = inboxSms,
                                            isLoading = isLoadingInbox,
                                            isSyncing = isSyncingMissed,
                                            statusMessage = manualForwardStatus,
                                            onRefreshInbox = { viewModel.loadInboxMessages() },
                                            onSyncAllMissed = { viewModel.syncMissedSms() },
                                            onForwardSms = { viewModel.forwardSingleInboxSms(it) },
                                            onDismissStatus = { viewModel.clearManualForwardStatus() }
                                        )
                                    }
                                    AppTab.SMTP -> {
                                        SmtpScreen(
                                            currentConfig = smtpConfig,
                                            isTesting = isTestingSmtp,
                                            testResult = smtpTestResult,
                                            onSaveConfig = { viewModel.saveSmtpConfig(it) },
                                            onTestConnection = { config, recipient ->
                                                viewModel.testSmtpConnection(config, recipient)
                                            },
                                            onClearTestResult = { viewModel.clearSmtpTestResult() }
                                        )
                                    }
                                    AppTab.LOGS -> {
                                        LogsScreen(
                                            logs = filteredLogs,
                                            totalCount = totalLogsCount,
                                            successCount = successLogsCount,
                                            currentFilter = logFilter,
                                            onFilterChanged = { viewModel.setLogFilter(it) },
                                            onClearLogs = { viewModel.clearLogs() }
                                        )
                                    }
                                    AppTab.SIMULATOR -> {
                                        SimulatorScreen(
                                            sender = simulatorSender,
                                            message = simulatorMessage,
                                            isSimulating = isSimulating,
                                            results = simulationResults,
                                            onSenderChange = { viewModel.updateSimulatorSender(it) },
                                            onMessageChange = { viewModel.updateSimulatorMessage(it) },
                                            onRunSimulation = { viewModel.runSimulator() },
                                            onClearResults = { viewModel.clearSimulationResults() },
                                            onNavigateToLogs = { selectedTab = AppTab.LOGS }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add/Edit Rule Dialog
                    editingRule?.let { rule ->
                        RuleDialog(
                            rule = rule,
                            isTesting = isTestingRule,
                            testResult = ruleTestResult,
                            onDismiss = { viewModel.closeRuleDialog() },
                            onSave = { viewModel.saveRule(it) },
                            onTest = { viewModel.testRuleDestination(it) }
                        )
                    }
                }
            }
        }
    }
}

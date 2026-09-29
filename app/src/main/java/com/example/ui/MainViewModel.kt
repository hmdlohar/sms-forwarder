package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DestinationType
import com.example.data.model.ForwardLog
import com.example.data.model.ForwardRule
import com.example.data.model.MatchMode
import com.example.data.model.SmtpConfig
import com.example.data.model.SmtpSecurityType
import com.example.data.repository.SmsForwarderRepository
import com.example.network.SmtpMailer
import com.example.service.ForwardingManager
import com.example.service.SmsForwarderService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LogFilter {
    ALL,
    SUCCESS,
    FAILED
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmsForwarderRepository(application)

    // Master Service Running State
    private val _isServiceRunning = MutableStateFlow(SmsForwarderService.isServiceRunning(application))
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    // Rules
    val rules: StateFlow<List<ForwardRule>> = repository.allRules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Editing Rule
    private val _editingRule = MutableStateFlow<ForwardRule?>(null)
    val editingRule: StateFlow<ForwardRule?> = _editingRule.asStateFlow()

    // Rule Test feedback
    private val _ruleTestResult = MutableStateFlow<String?>(null)
    val ruleTestResult: StateFlow<String?> = _ruleTestResult.asStateFlow()

    private val _isTestingRule = MutableStateFlow(false)
    val isTestingRule: StateFlow<Boolean> = _isTestingRule.asStateFlow()

    // SMTP Config
    val smtpConfig: StateFlow<SmtpConfig> = repository.smtpConfig
        .combine(MutableStateFlow(Unit)) { cfg, _ ->
            cfg ?: SmtpConfig()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SmtpConfig()
        )

    private val _isTestingSmtp = MutableStateFlow(false)
    val isTestingSmtp: StateFlow<Boolean> = _isTestingSmtp.asStateFlow()

    private val _smtpTestResult = MutableStateFlow<SmtpMailer.SmtpResult?>(null)
    val smtpTestResult: StateFlow<SmtpMailer.SmtpResult?> = _smtpTestResult.asStateFlow()

    // Logs
    private val _logFilter = MutableStateFlow(LogFilter.ALL)
    val logFilter: StateFlow<LogFilter> = _logFilter.asStateFlow()

    private val _rawLogs = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredLogs: StateFlow<List<ForwardLog>> = combine(_rawLogs, _logFilter) { list, filter ->
        when (filter) {
            LogFilter.ALL -> list
            LogFilter.SUCCESS -> list.filter { it.isSuccess }
            LogFilter.FAILED -> list.filter { !it.isSuccess }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalLogsCount: StateFlow<Int> = repository.totalLogsCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val successLogsCount: StateFlow<Int> = repository.successLogsCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Simulation
    private val _simulatorSender = MutableStateFlow("+14155550199")
    val simulatorSender: StateFlow<String> = _simulatorSender.asStateFlow()

    private val _simulatorMessage = MutableStateFlow("Your OTP verification code is 849201. Do not share this.")
    val simulatorMessage: StateFlow<String> = _simulatorMessage.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _simulationResults = MutableStateFlow<List<ForwardingManager.ForwardExecutionResult>?>(null)
    val simulationResults: StateFlow<List<ForwardingManager.ForwardExecutionResult>?> = _simulationResults.asStateFlow()

    // Service Toggle
    fun toggleService(enable: Boolean) {
        val app = getApplication<Application>()
        if (enable) {
            SmsForwarderService.startService(app)
        } else {
            SmsForwarderService.stopService(app)
        }
        _isServiceRunning.value = enable
    }

    // Rules Management
    fun openNewRuleDialog() {
        _editingRule.value = ForwardRule(
            id = 0,
            name = "",
            isEnabled = true,
            senderMatchMode = MatchMode.ANY,
            senderPattern = "",
            contentMatchMode = MatchMode.CONTAINS,
            contentPattern = "",
            destinationType = DestinationType.WEBHOOK,
            webhookUrl = "https://webhook.site/",
            webhookMethod = "POST",
            recipientEmail = "",
            emailSubject = "SMS from {sender}"
        )
        _ruleTestResult.value = null
    }

    fun openEditRuleDialog(rule: ForwardRule) {
        _editingRule.value = rule
        _ruleTestResult.value = null
    }

    fun closeRuleDialog() {
        _editingRule.value = null
        _ruleTestResult.value = null
        _isTestingRule.value = false
    }

    fun saveRule(rule: ForwardRule) {
        viewModelScope.launch {
            repository.saveRule(rule)
            closeRuleDialog()
        }
    }

    fun setRuleEnabled(ruleId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setRuleEnabled(ruleId, isEnabled)
        }
    }

    fun deleteRule(rule: ForwardRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    fun testRuleDestination(rule: ForwardRule) {
        viewModelScope.launch {
            _isTestingRule.value = true
            _ruleTestResult.value = "Testing destination connection..."
            try {
                when (rule.destinationType) {
                    DestinationType.WEBHOOK -> {
                        if (rule.webhookUrl.isBlank()) {
                            _ruleTestResult.value = "Error: Webhook URL is empty"
                            _isTestingRule.value = false
                            return@launch
                        }
                        val result = repository.testWebhook(
                            url = rule.webhookUrl,
                            method = rule.webhookMethod,
                            headers = rule.webhookHeaders
                        )
                        _ruleTestResult.value = if (result.isSuccess) {
                            "Success: ${result.getOrNull()}"
                        } else {
                            "Failed: ${result.exceptionOrNull()?.message}"
                        }
                    }
                    DestinationType.EMAIL -> {
                        if (rule.recipientEmail.isBlank()) {
                            _ruleTestResult.value = "Error: Recipient email is empty"
                            _isTestingRule.value = false
                            return@launch
                        }
                        val config = repository.getSmtpConfigDirect()
                        if (config == null || config.host.isBlank()) {
                            _ruleTestResult.value = "Error: SMTP configuration not set. Please set up SMTP first."
                            _isTestingRule.value = false
                            return@launch
                        }
                        val res = repository.testSmtp(config, rule.recipientEmail)
                        _ruleTestResult.value = if (res.success) {
                            "Success: ${res.message}"
                        } else {
                            "SMTP Failed: ${res.message}"
                        }
                    }
                }
            } catch (e: Exception) {
                _ruleTestResult.value = "Error: ${e.message}"
            } finally {
                _isTestingRule.value = false
            }
        }
    }

    // SMTP Management
    fun saveSmtpConfig(config: SmtpConfig) {
        viewModelScope.launch {
            repository.saveSmtpConfig(config)
        }
    }

    fun testSmtpConnection(config: SmtpConfig, recipient: String) {
        viewModelScope.launch {
            _isTestingSmtp.value = true
            _smtpTestResult.value = null
            try {
                val res = repository.testSmtp(config, recipient)
                _smtpTestResult.value = res
            } catch (e: Exception) {
                _smtpTestResult.value = SmtpMailer.SmtpResult(
                    success = false,
                    message = "Connection exception: ${e.message}",
                    transcript = listOf("Error: ${e.localizedMessage}")
                )
            } finally {
                _isTestingSmtp.value = false
            }
        }
    }

    fun clearSmtpTestResult() {
        _smtpTestResult.value = null
    }

    // Logs Management
    fun setLogFilter(filter: LogFilter) {
        _logFilter.value = filter
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearAllLogs()
        }
    }

    // Simulator
    fun updateSimulatorSender(value: String) {
        _simulatorSender.value = value
    }

    fun updateSimulatorMessage(value: String) {
        _simulatorMessage.value = value
    }

    fun runSimulator() {
        viewModelScope.launch {
            _isSimulating.value = true
            _simulationResults.value = null
            try {
                val results = repository.simulateSms(_simulatorSender.value, _simulatorMessage.value)
                _simulationResults.value = results
            } finally {
                _isSimulating.value = false
            }
        }
    }

    fun clearSimulationResults() {
        _simulationResults.value = null
    }

    // Device Inbox & Sync Missed SMS
    private val _inboxSms = MutableStateFlow<List<com.example.data.model.DeviceSms>>(emptyList())
    val inboxSms: StateFlow<List<com.example.data.model.DeviceSms>> = _inboxSms.asStateFlow()

    private val _isLoadingInbox = MutableStateFlow(false)
    val isLoadingInbox: StateFlow<Boolean> = _isLoadingInbox.asStateFlow()

    private val _isSyncingMissed = MutableStateFlow(false)
    val isSyncingMissed: StateFlow<Boolean> = _isSyncingMissed.asStateFlow()

    private val _manualForwardStatus = MutableStateFlow<String?>(null)
    val manualForwardStatus: StateFlow<String?> = _manualForwardStatus.asStateFlow()

    fun loadInboxMessages() {
        viewModelScope.launch {
            _isLoadingInbox.value = true
            try {
                val list = repository.loadDeviceInbox(100)
                _inboxSms.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingInbox.value = false
            }
        }
    }

    fun forwardSingleInboxSms(sms: com.example.data.model.DeviceSms) {
        viewModelScope.launch {
            _manualForwardStatus.value = "Forwarding SMS from ${sms.address}..."
            try {
                val results = repository.forwardSpecificSms(sms.address, sms.body, sms.date)
                if (results.isEmpty()) {
                    _manualForwardStatus.value = "No active rule matched this SMS (${sms.address}). Check rule conditions."
                } else {
                    val successCount = results.count { it.isSuccess }
                    _manualForwardStatus.value = "Forwarded to $successCount destination(s)!"
                }
            } catch (e: Exception) {
                _manualForwardStatus.value = "Error forwarding: ${e.message}"
            }
        }
    }

    fun syncMissedSms() {
        viewModelScope.launch {
            _isSyncingMissed.value = true
            _manualForwardStatus.value = "Scanning inbox for missed SMS..."
            try {
                val inbox = repository.loadDeviceInbox(50)
                var totalTriggered = 0
                for (sms in inbox) {
                    val results = repository.forwardSpecificSms(sms.address, sms.body, sms.date)
                    if (results.isNotEmpty()) {
                        totalTriggered += results.count { it.isSuccess }
                    }
                }
                _manualForwardStatus.value = "Sync complete: forwarded $totalTriggered matching SMS destination(s)."
                loadInboxMessages()
            } catch (e: Exception) {
                _manualForwardStatus.value = "Sync failed: ${e.message}"
            } finally {
                _isSyncingMissed.value = false
            }
        }
    }

    fun clearManualForwardStatus() {
        _manualForwardStatus.value = null
    }
}

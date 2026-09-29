package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.DestinationType
import com.example.data.model.ForwardRule
import com.example.data.model.MatchMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleDialog(
    rule: ForwardRule,
    isTesting: Boolean,
    testResult: String?,
    onDismiss: () -> Unit,
    onSave: (ForwardRule) -> Unit,
    onTest: (ForwardRule) -> Unit
) {
    var name by remember(rule) { mutableStateOf(rule.name) }
    var isEnabled by remember(rule) { mutableStateOf(rule.isEnabled) }

    var senderMatchMode by remember(rule) { mutableStateOf(rule.senderMatchMode) }
    var senderPattern by remember(rule) { mutableStateOf(rule.senderPattern) }

    var contentMatchMode by remember(rule) { mutableStateOf(rule.contentMatchMode) }
    var contentPattern by remember(rule) { mutableStateOf(rule.contentPattern) }

    var destinationType by remember(rule) { mutableStateOf(rule.destinationType) }

    var webhookUrl by remember(rule) { mutableStateOf(rule.webhookUrl) }
    var webhookMethod by remember(rule) { mutableStateOf(rule.webhookMethod) }
    var webhookHeaders by remember(rule) { mutableStateOf(rule.webhookHeaders) }

    var recipientEmail by remember(rule) { mutableStateOf(rule.recipientEmail) }
    var emailSubject by remember(rule) { mutableStateOf(rule.emailSubject) }

    var nameError by remember { mutableStateOf(false) }
    var destinationError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (rule.id == 0L) "New Forwarding Rule" else "Edit Rule",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rule Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Rule Name") },
                    placeholder = { Text("e.g. Bank OTPs to Webhook") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Rule name is required") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_name_input")
                )

                // Sender Condition
                Text("Sender Match Condition", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var senderDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = senderDropdownExpanded,
                        onExpandedChange = { senderDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = senderMatchMode.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mode") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = senderDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = senderDropdownExpanded,
                            onDismissRequest = { senderDropdownExpanded = false }
                        ) {
                            MatchMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.name) },
                                    onClick = {
                                        senderMatchMode = mode
                                        senderDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (senderMatchMode != MatchMode.ANY) {
                        OutlinedTextField(
                            value = senderPattern,
                            onValueChange = { senderPattern = it },
                            label = { Text("Pattern / Number") },
                            placeholder = { Text("e.g. +1415 or BANK") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }

                // Content Condition
                Text("Message Content Match", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var contentDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = contentDropdownExpanded,
                        onExpandedChange = { contentDropdownExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = contentMatchMode.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mode") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contentDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = contentDropdownExpanded,
                            onDismissRequest = { contentDropdownExpanded = false }
                        ) {
                            MatchMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.name) },
                                    onClick = {
                                        contentMatchMode = mode
                                        contentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (contentMatchMode != MatchMode.ANY) {
                        OutlinedTextField(
                            value = contentPattern,
                            onValueChange = { contentPattern = it },
                            label = { Text("Keyword / Regex") },
                            placeholder = { Text("e.g. OTP or code") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }

                // Destination Type Selector
                Text("Forwarding Destination", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = destinationType == DestinationType.WEBHOOK,
                        onClick = { destinationType = DestinationType.WEBHOOK },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = { Icon(Icons.Default.Http, contentDescription = null) }
                    ) {
                        Text("Webhook")
                    }
                    SegmentedButton(
                        selected = destinationType == DestinationType.EMAIL,
                        onClick = { destinationType = DestinationType.EMAIL },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) }
                    ) {
                        Text("Email (SMTP)")
                    }
                }

                // Webhook details
                if (destinationType == DestinationType.WEBHOOK) {
                    OutlinedTextField(
                        value = webhookUrl,
                        onValueChange = {
                            webhookUrl = it
                            destinationError = it.isBlank()
                        },
                        label = { Text("Webhook URL") },
                        placeholder = { Text("https://example.com/api/sms-webhook") },
                        isError = destinationError,
                        supportingText = if (destinationError) {
                            { Text("Valid URL is required") }
                        } else {
                            { Text("Incoming SMS JSON will be sent to this endpoint") }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("webhook_url_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = webhookMethod == "POST",
                            onClick = { webhookMethod = "POST" },
                            label = { Text("POST (JSON Body)") }
                        )
                        FilterChip(
                            selected = webhookMethod == "GET",
                            onClick = { webhookMethod = "GET" },
                            label = { Text("GET (URL Query)") }
                        )
                    }

                    OutlinedTextField(
                        value = webhookHeaders,
                        onValueChange = { webhookHeaders = it },
                        label = { Text("Custom Headers (Optional)") },
                        placeholder = { Text("Authorization: Bearer token123\nX-App-Secret: secret") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                // Email details
                if (destinationType == DestinationType.EMAIL) {
                    OutlinedTextField(
                        value = recipientEmail,
                        onValueChange = {
                            recipientEmail = it
                            destinationError = it.isBlank()
                        },
                        label = { Text("Recipient Email Address") },
                        placeholder = { Text("notifications@yourdomain.com") },
                        isError = destinationError,
                        supportingText = if (destinationError) {
                            { Text("Recipient email is required") }
                        } else {
                            { Text("Requires SMTP to be configured in the SMTP tab") }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recipient_email_input")
                    )

                    OutlinedTextField(
                        value = emailSubject,
                        onValueChange = { emailSubject = it },
                        label = { Text("Subject Template") },
                        placeholder = { Text("SMS from {sender}") },
                        supportingText = { Text("Variables: {sender}, {rule}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Inline Test feedback
                if (testResult != null) {
                    val isSuccess = testResult.startsWith("Success")
                    Surface(
                        color = if (isSuccess) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isSuccess) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = testResult,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSuccess) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Test Destination Button
                OutlinedButton(
                    onClick = {
                        val currentDraft = rule.copy(
                            name = name.ifBlank { "Test Rule" },
                            senderMatchMode = senderMatchMode,
                            senderPattern = senderPattern,
                            contentMatchMode = contentMatchMode,
                            contentPattern = contentPattern,
                            destinationType = destinationType,
                            webhookUrl = webhookUrl,
                            webhookMethod = webhookMethod,
                            webhookHeaders = webhookHeaders,
                            recipientEmail = recipientEmail,
                            emailSubject = emailSubject
                        )
                        onTest(currentDraft)
                    },
                    enabled = !isTesting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testing Connection...")
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Destination Now")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    if (destinationType == DestinationType.WEBHOOK && webhookUrl.isBlank()) {
                        destinationError = true
                        return@Button
                    }
                    if (destinationType == DestinationType.EMAIL && recipientEmail.isBlank()) {
                        destinationError = true
                        return@Button
                    }

                    onSave(
                        rule.copy(
                            name = name.trim(),
                            isEnabled = isEnabled,
                            senderMatchMode = senderMatchMode,
                            senderPattern = senderPattern.trim(),
                            contentMatchMode = contentMatchMode,
                            contentPattern = contentPattern.trim(),
                            destinationType = destinationType,
                            webhookUrl = webhookUrl.trim(),
                            webhookMethod = webhookMethod,
                            webhookHeaders = webhookHeaders.trim(),
                            recipientEmail = recipientEmail.trim(),
                            emailSubject = emailSubject.trim()
                        )
                    )
                },
                modifier = Modifier.testTag("save_rule_button")
            ) {
                Text("Save Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

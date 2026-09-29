package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SmtpConfig
import com.example.data.model.SmtpSecurityType
import com.example.network.SmtpMailer

@Composable
fun SmtpScreen(
    currentConfig: SmtpConfig,
    isTesting: Boolean,
    testResult: SmtpMailer.SmtpResult?,
    onSaveConfig: (SmtpConfig) -> Unit,
    onTestConnection: (SmtpConfig, String) -> Unit,
    onClearTestResult: () -> Unit
) {
    var host by remember(currentConfig) { mutableStateOf(currentConfig.host) }
    var port by remember(currentConfig) { mutableStateOf(currentConfig.port.toString()) }
    var securityType by remember(currentConfig) { mutableStateOf(currentConfig.securityType) }
    var username by remember(currentConfig) { mutableStateOf(currentConfig.username) }
    var password by remember(currentConfig) { mutableStateOf(currentConfig.password) }
    var fromEmail by remember(currentConfig) { mutableStateOf(currentConfig.fromEmail) }
    var fromName by remember(currentConfig) { mutableStateOf(currentConfig.fromName) }

    var passwordVisible by remember { mutableStateOf(false) }
    var testRecipient by remember { mutableStateOf("") }
    var showTranscript by remember { mutableStateOf(false) }
    var savedFeedback by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Info Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Mail,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "SMTP Email Forwarding Setup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configure your outgoing mail server to forward incoming SMS messages directly to any email address. For Gmail or Outlook, use an App Password.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Presets
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = host == "smtp.gmail.com" && port == "587",
                        onClick = {
                            host = "smtp.gmail.com"
                            port = "587"
                            securityType = SmtpSecurityType.STARTTLS
                        },
                        label = { Text("Gmail") }
                    )
                    FilterChip(
                        selected = host == "smtp.office365.com" && port == "587",
                        onClick = {
                            host = "smtp.office365.com"
                            port = "587"
                            securityType = SmtpSecurityType.STARTTLS
                        },
                        label = { Text("Outlook / 365") }
                    )
                    FilterChip(
                        selected = host == "smtp.mail.yahoo.com" && port == "465",
                        onClick = {
                            host = "smtp.mail.yahoo.com"
                            port = "465"
                            securityType = SmtpSecurityType.SSL_TLS
                        },
                        label = { Text("Yahoo") }
                    )
                }
            }
        }

        // Server Settings Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Server Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = host,
                            onValueChange = { host = it },
                            label = { Text("SMTP Host") },
                            placeholder = { Text("smtp.example.com") },
                            modifier = Modifier
                                .weight(2f)
                                .testTag("smtp_host_input")
                        )
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text("Port") },
                            placeholder = { Text("587") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("smtp_port_input")
                        )
                    }

                    // Security Mode
                    Text("Security Type", style = MaterialTheme.typography.labelMedium)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = securityType == SmtpSecurityType.STARTTLS,
                            onClick = { securityType = SmtpSecurityType.STARTTLS },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                        ) {
                            Text("STARTTLS (587)")
                        }
                        SegmentedButton(
                            selected = securityType == SmtpSecurityType.SSL_TLS,
                            onClick = { securityType = SmtpSecurityType.SSL_TLS },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                        ) {
                            Text("SSL/TLS (465)")
                        }
                        SegmentedButton(
                            selected = securityType == SmtpSecurityType.PLAIN,
                            onClick = { securityType = SmtpSecurityType.PLAIN },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                        ) {
                            Text("None (25)")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Authentication & Sender Identity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username / Email") },
                        placeholder = { Text("you@gmail.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("smtp_username_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password / App Password") },
                        placeholder = { Text("App password or account password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        supportingText = {
                            Text("Gmail users: generate an App Password in Google Account Settings > Security")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("smtp_password_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = fromEmail,
                            onValueChange = { fromEmail = it },
                            label = { Text("From Email") },
                            placeholder = { Text("you@gmail.com") },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("smtp_from_email_input")
                        )
                        OutlinedTextField(
                            value = fromName,
                            onValueChange = { fromName = it },
                            label = { Text("Sender Name") },
                            placeholder = { Text("SMS Forwarder") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("smtp_from_name_input")
                        )
                    }

                    Button(
                        onClick = {
                            val newConfig = SmtpConfig(
                                id = 1,
                                host = host.trim(),
                                port = port.trim().toIntOrNull() ?: 587,
                                username = username.trim(),
                                password = password,
                                fromEmail = fromEmail.trim(),
                                fromName = fromName.trim(),
                                securityType = securityType
                            )
                            onSaveConfig(newConfig)
                            savedFeedback = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_smtp_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (savedFeedback) "Settings Saved!" else "Save SMTP Settings")
                    }
                }
            }
        }

        // Test Connection & Send Email
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Verify Connection & Send Test Email",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Send a test message through this SMTP server to confirm your credentials and firewall settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = testRecipient,
                        onValueChange = { testRecipient = it },
                        label = { Text("Test Recipient Email") },
                        placeholder = { Text("your-personal-email@example.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_recipient_email_input")
                    )

                    OutlinedButton(
                        onClick = {
                            val currentDraft = SmtpConfig(
                                id = 1,
                                host = host.trim(),
                                port = port.trim().toIntOrNull() ?: 587,
                                username = username.trim(),
                                password = password,
                                fromEmail = fromEmail.trim(),
                                fromName = fromName.trim(),
                                securityType = securityType
                            )
                            val target = testRecipient.ifBlank { username }
                            onTestConnection(currentDraft, target)
                        },
                        enabled = !isTesting && host.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_test_email_button")
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connecting & Sending...")
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Test Email Now")
                        }
                    }

                    // Test result display
                    testResult?.let { result ->
                        Surface(
                            color = if (result.success) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (result.success) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (result.success) "Email Sent Successfully!" else "SMTP Connection Failed",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (result.success) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = result.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (result.success) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onErrorContainer
                                )

                                if (result.transcript.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(
                                        onClick = { showTranscript = !showTranscript },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = if (showTranscript) "Hide Server Transcript" else "View Server Transcript (${result.transcript.size} lines)",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }

                                    AnimatedVisibility(visible = showTranscript) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                result.transcript.forEach { line ->
                                                    Text(
                                                        text = line,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 11.sp,
                                                        color = if (line.startsWith("S:")) MaterialTheme.colorScheme.primary
                                                        else if (line.startsWith("Error")) MaterialTheme.colorScheme.error
                                                        else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

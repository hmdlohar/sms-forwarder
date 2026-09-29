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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DestinationType
import com.example.service.ForwardingManager

@Composable
fun SimulatorScreen(
    sender: String,
    message: String,
    isSimulating: Boolean,
    results: List<ForwardingManager.ForwardExecutionResult>?,
    onSenderChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onRunSimulation: () -> Unit,
    onClearResults: () -> Unit,
    onNavigateToLogs: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info Banner
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
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
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Incoming SMS Testing Simulator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Test your rule matching, Webhook endpoints, and SMTP email delivery immediately without waiting for a real phone SMS.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Preset Templates
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Sample Scenarios",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = sender == "BANK_ALERT",
                            onClick = {
                                onSenderChange("BANK_ALERT")
                                onMessageChange("Your OTP verification code is 492108. Valid for 5 minutes.")
                            },
                            label = { Text("Bank OTP") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = sender == "ALERT_OPS",
                            onClick = {
                                onSenderChange("ALERT_OPS")
                                onMessageChange("ALERT: DB connection spike detected on us-east cluster.")
                            },
                            label = { Text("Server Alert") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = sender == "+14155550199",
                            onClick = {
                                onSenderChange("+14155550199")
                                onMessageChange("Hi, I sent the invoice for the project to your email.")
                            },
                            label = { Text("Personal SMS") }
                        )
                    }
                }
            }
        }

        // Input Card
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
                        text = "Simulated SMS Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = sender,
                        onValueChange = onSenderChange,
                        label = { Text("Originating Sender / Phone Number") },
                        placeholder = { Text("e.g. +14155550199 or BANK_OTP") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Sms, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulator_sender_input")
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = onMessageChange,
                        label = { Text("Message Body Content") },
                        placeholder = { Text("Enter SMS text to test matching...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulator_message_input")
                    )

                    Button(
                        onClick = onRunSimulation,
                        enabled = !isSimulating && sender.isNotBlank() && message.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_simulation_button")
                    ) {
                        if (isSimulating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing Rules & Forwarding...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulate Incoming SMS Now")
                        }
                    }
                }
            }
        }

        // Results Card
        item {
            results?.let { executionResults ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Simulation Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = onClearResults) {
                                Text("Dismiss")
                            }
                        }

                        if (executionResults.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No active rules matched this sender and message content. Verify your rule patterns in the Rules tab.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "${executionResults.size} rule${if (executionResults.size > 1) "s" else ""} triggered:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )

                            executionResults.forEach { result ->
                                OutlinedCard(
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (result.destinationType == DestinationType.WEBHOOK) Icons.Default.Http
                                                    else Icons.Default.AlternateEmail,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = result.rule.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Surface(
                                                color = if (result.isSuccess) MaterialTheme.colorScheme.tertiaryContainer
                                                else MaterialTheme.colorScheme.errorContainer,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (result.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                                        contentDescription = null,
                                                        tint = if (result.isSuccess) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = if (result.isSuccess) "Delivered" else "Failed",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (result.isSuccess) MaterialTheme.colorScheme.onTertiaryContainer
                                                        else MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "Target: ${result.destination}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = result.message,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(8.dp),
                                                color = if (result.isSuccess) MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = onNavigateToLogs,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View in Forwarding History")
                            }
                        }
                    }
                }
            }
        }
    }
}

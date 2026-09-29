package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forward_rules")
data class ForwardRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isEnabled: Boolean = true,
    // Match conditions
    val senderMatchMode: MatchMode = MatchMode.ANY,
    val senderPattern: String = "",
    val contentMatchMode: MatchMode = MatchMode.ANY,
    val contentPattern: String = "",
    // Destination configuration
    val destinationType: DestinationType = DestinationType.WEBHOOK,
    // Webhook details
    val webhookUrl: String = "",
    val webhookMethod: String = "POST",
    val webhookHeaders: String = "", // formatted as key: value pairs per line
    // Email details
    val recipientEmail: String = "",
    val emailSubject: String = "SMS from {sender}",
    // Metrics
    val matchesCount: Int = 0,
    val lastMatchedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

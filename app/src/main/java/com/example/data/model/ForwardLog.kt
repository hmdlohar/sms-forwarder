package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forward_logs")
data class ForwardLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleId: Long? = null,
    val ruleName: String,
    val sender: String,
    val smsBody: String,
    val destinationType: DestinationType,
    val destination: String,
    val isSuccess: Boolean,
    val responseStatus: String,
    val timestamp: Long = System.currentTimeMillis()
)

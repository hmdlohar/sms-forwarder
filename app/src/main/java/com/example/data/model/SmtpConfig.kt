package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smtp_config")
data class SmtpConfig(
    @PrimaryKey
    val id: Int = 1,
    val host: String = "smtp.gmail.com",
    val port: Int = 587,
    val username: String = "",
    val password: String = "",
    val fromEmail: String = "",
    val fromName: String = "SMS Forwarder",
    val securityType: SmtpSecurityType = SmtpSecurityType.STARTTLS
)

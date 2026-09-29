package com.example.data.model

enum class SmtpSecurityType {
    STARTTLS, // Port 587 default
    SSL_TLS,  // Port 465 default
    PLAIN     // Port 25 default
}

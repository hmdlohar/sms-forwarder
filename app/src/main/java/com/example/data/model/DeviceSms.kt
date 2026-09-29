package com.example.data.model

data class DeviceSms(
    val id: Long,
    val address: String,
    val body: String,
    val date: Long,
    val isForwarded: Boolean = false
)

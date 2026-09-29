package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.model.DeviceSms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsInboxReader(private val context: Context) {

    suspend fun readRecentInboxSms(limit: Int = 50): List<DeviceSms> = withContext(Dispatchers.IO) {
        val smsList = mutableListOf<DeviceSms>()
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "date DESC LIMIT $limit"
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow("_id")
                val addrCol = it.getColumnIndexOrThrow("address")
                val bodyCol = it.getColumnIndexOrThrow("body")
                val dateCol = it.getColumnIndexOrThrow("date")

                while (it.moveToNext()) {
                    val id = it.getLong(idCol)
                    val address = it.getString(addrCol) ?: "Unknown"
                    val body = it.getString(bodyCol) ?: ""
                    val date = it.getLong(dateCol)

                    smsList.add(
                        DeviceSms(
                            id = id,
                            address = address,
                            body = body,
                            date = date,
                            isForwarded = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        smsList
    }
}

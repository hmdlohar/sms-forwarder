package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class WebhookSender(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {

    suspend fun sendWebhook(
        url: String,
        method: String,
        headersText: String,
        sender: String,
        message: String,
        timestamp: Long,
        ruleName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            val isoTime = isoFormat.format(Date(timestamp))

            val jsonObject = JSONObject().apply {
                put("sender", sender)
                put("message", message)
                put("timestamp", timestamp)
                put("timestampIso", isoTime)
                put("ruleName", ruleName)
            }
            val jsonPayload = jsonObject.toString()

            val headerBuilder = Headers.Builder()
            if (headersText.isNotBlank()) {
                headersText.lines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.contains(":")) {
                        val parts = trimmed.split(":", limit = 2)
                        val key = parts[0].trim()
                        val value = parts[1].trim()
                        if (key.isNotEmpty()) {
                            headerBuilder.add(key, value)
                        }
                    }
                }
            }

            val requestBuilder = Request.Builder()
            val cleanMethod = method.trim().uppercase()

            if (cleanMethod == "GET") {
                val httpUrl = url.toHttpUrlOrNull()
                    ?: return@withContext Result.failure(IllegalArgumentException("Invalid URL: $url"))

                val finalUrl = httpUrl.newBuilder()
                    .addQueryParameter("sender", sender)
                    .addQueryParameter("message", message)
                    .addQueryParameter("timestamp", timestamp.toString())
                    .addQueryParameter("rule", ruleName)
                    .build()

                requestBuilder.url(finalUrl).get()
            } else {
                // Default to POST
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = jsonPayload.toRequestBody(mediaType)
                requestBuilder.url(url).post(body)
                if (headerBuilder.get("Content-Type") == null) {
                    headerBuilder.add("Content-Type", "application/json")
                }
            }

            requestBuilder.headers(headerBuilder.build())
            val response = client.newCall(requestBuilder.build()).execute()

            val code = response.code
            val responseBody = response.body?.string()?.take(200) ?: ""
            response.close()

            if (response.isSuccessful) {
                Result.success("HTTP $code Success${if (responseBody.isNotBlank()) ": $responseBody" else ""}")
            } else {
                Result.failure(Exception("HTTP $code Error: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testEndpoint(
        url: String,
        method: String,
        headersText: String
    ): Result<String> {
        return sendWebhook(
            url = url,
            method = method,
            headersText = headersText,
            sender = "+19995550123",
            message = "Test message from SMS Forwarder verification",
            timestamp = System.currentTimeMillis(),
            ruleName = "Test Rule"
        )
    }
}

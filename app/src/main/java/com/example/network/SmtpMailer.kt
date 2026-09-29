package com.example.network

import android.util.Base64
import com.example.data.model.SmtpConfig
import com.example.data.model.SmtpSecurityType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class SmtpMailer {

    data class SmtpResult(
        val success: Boolean,
        val message: String,
        val transcript: List<String> = emptyList()
    )

    suspend fun sendEmail(
        config: SmtpConfig,
        recipientEmail: String,
        subject: String,
        body: String,
        isHtml: Boolean = false
    ): SmtpResult = withContext(Dispatchers.IO) {
        val transcript = mutableListOf<String>()

        if (config.host.isBlank()) {
            return@withContext SmtpResult(false, "SMTP host is empty", transcript)
        }
        if (recipientEmail.isBlank()) {
            return@withContext SmtpResult(false, "Recipient email is empty", transcript)
        }

        var socket: Socket? = null
        try {
            val timeout = 15000 // 15 seconds

            // Connect socket based on security type
            if (config.securityType == SmtpSecurityType.SSL_TLS) {
                transcript.add("Connecting via SSL/TLS to ${config.host}:${config.port}...")
                val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
                val sslSocket = sslFactory.createSocket() as SSLSocket
                sslSocket.connect(InetSocketAddress(config.host, config.port), timeout)
                sslSocket.soTimeout = timeout
                sslSocket.startHandshake()
                socket = sslSocket
            } else {
                transcript.add("Connecting to ${config.host}:${config.port}...")
                val plainSocket = Socket()
                plainSocket.connect(InetSocketAddress(config.host, config.port), timeout)
                plainSocket.soTimeout = timeout
                socket = plainSocket
            }

            var reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
            var writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8), true)

            // 1. Read initial banner (expect 220)
            val banner = readResponse(reader, transcript)
            if (!banner.startsWith("220")) {
                socket.close()
                return@withContext SmtpResult(false, "Unexpected server banner: $banner", transcript)
            }

            // 2. Send EHLO
            val localhost = "127.0.0.1"
            sendCommand(writer, "EHLO $localhost", transcript)
            val ehloResp = readResponse(reader, transcript)

            // 3. Handle STARTTLS if configured
            if (config.securityType == SmtpSecurityType.STARTTLS) {
                sendCommand(writer, "STARTTLS", transcript)
                val startTlsResp = readResponse(reader, transcript)
                if (!startTlsResp.startsWith("220")) {
                    socket.close()
                    return@withContext SmtpResult(false, "STARTTLS rejected: $startTlsResp", transcript)
                }

                transcript.add("Upgrading to TLS...")
                val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
                val sslSocket = sslFactory.createSocket(socket, config.host, config.port, true) as SSLSocket
                sslSocket.soTimeout = timeout
                sslSocket.startHandshake()
                socket = sslSocket

                reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
                writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8), true)

                // Resend EHLO after TLS upgrade
                sendCommand(writer, "EHLO $localhost", transcript)
                readResponse(reader, transcript)
            }

            // 4. Authenticate if username is provided
            if (config.username.isNotBlank()) {
                sendCommand(writer, "AUTH LOGIN", transcript)
                val authResp = readResponse(reader, transcript)
                if (!authResp.startsWith("334")) {
                    socket.close()
                    return@withContext SmtpResult(false, "AUTH LOGIN failed: $authResp", transcript)
                }

                // Send Base64 Username
                val b64User = Base64.encodeToString(config.username.trim().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                sendCommand(writer, b64User, transcript, mask = false)
                val userResp = readResponse(reader, transcript)
                if (!userResp.startsWith("334")) {
                    socket.close()
                    return@withContext SmtpResult(false, "Username rejected: $userResp", transcript)
                }

                // Send Base64 Password
                val b64Pass = Base64.encodeToString(config.password.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                sendCommand(writer, b64Pass, transcript, mask = true)
                val passResp = readResponse(reader, transcript)
                if (!passResp.startsWith("235")) {
                    socket.close()
                    return@withContext SmtpResult(false, "Password/Credentials rejected: $passResp (Check App Password if using Gmail/Outlook)", transcript)
                }
            }

            // 5. MAIL FROM
            val fromAddress = if (config.fromEmail.isNotBlank()) config.fromEmail.trim() else config.username.trim()
            sendCommand(writer, "MAIL FROM:<$fromAddress>", transcript)
            val mailFromResp = readResponse(reader, transcript)
            if (!mailFromResp.startsWith("250")) {
                socket.close()
                return@withContext SmtpResult(false, "MAIL FROM rejected: $mailFromResp", transcript)
            }

            // 6. RCPT TO
            val cleanRecipient = recipientEmail.trim()
            sendCommand(writer, "RCPT TO:<$cleanRecipient>", transcript)
            val rcptToResp = readResponse(reader, transcript)
            if (!rcptToResp.startsWith("250")) {
                socket.close()
                return@withContext SmtpResult(false, "RCPT TO rejected: $rcptToResp", transcript)
            }

            // 7. DATA
            sendCommand(writer, "DATA", transcript)
            val dataResp = readResponse(reader, transcript)
            if (!dataResp.startsWith("354")) {
                socket.close()
                return@withContext SmtpResult(false, "DATA command rejected: $dataResp", transcript)
            }

            // 8. Headers & Email Body
            val rfcDate = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).format(Date())
            val fromHeader = if (config.fromName.isNotBlank()) {
                "\"${config.fromName.replace("\"", "")}\" <$fromAddress>"
            } else {
                "<$fromAddress>"
            }

            // Escape '.' at the beginning of lines per SMTP RFC
            val escapedBody = body.lines().joinToString("\r\n") { line ->
                if (line.startsWith(".")) ".$line" else line
            }

            val contentType = if (isHtml) "text/html; charset=UTF-8" else "text/plain; charset=UTF-8"

            val rawMessage = buildString {
                append("From: ").append(fromHeader).append("\r\n")
                append("To: <").append(cleanRecipient).append(">\r\n")
                append("Date: ").append(rfcDate).append("\r\n")
                append("Subject: =?UTF-8?B?")
                    .append(Base64.encodeToString(subject.toByteArray(Charsets.UTF_8), Base64.NO_WRAP))
                    .append("?=\r\n")
                append("MIME-Version: 1.0\r\n")
                append("Content-Type: ").append(contentType).append("\r\n")
                append("Content-Transfer-Encoding: 8bit\r\n")
                append("\r\n")
                append(escapedBody).append("\r\n")
                append(".")
            }

            writer.print(rawMessage + "\r\n")
            writer.flush()
            transcript.add("Sent email payload (${rawMessage.length} bytes)")

            val finalResp = readResponse(reader, transcript)
            if (!finalResp.startsWith("250")) {
                socket.close()
                return@withContext SmtpResult(false, "Message delivery error: $finalResp", transcript)
            }

            // 9. QUIT
            try {
                sendCommand(writer, "QUIT", transcript)
                readResponse(reader, transcript)
            } catch (_: Exception) {}

            socket.close()
            SmtpResult(true, "Email sent successfully: $finalResp", transcript)
        } catch (e: Exception) {
            try { socket?.close() } catch (_: Exception) {}
            transcript.add("Error: ${e.localizedMessage}")
            SmtpResult(false, "SMTP Error: ${e.message ?: e.javaClass.simpleName}", transcript)
        }
    }

    private fun sendCommand(writer: PrintWriter, cmd: String, transcript: MutableList<String>, mask: Boolean = false) {
        writer.print(cmd + "\r\n")
        writer.flush()
        if (mask) {
            transcript.add("C: [PROTECTED PASSWORD]")
        } else {
            transcript.add("C: $cmd")
        }
    }

    private fun readResponse(reader: BufferedReader, transcript: MutableList<String>): String {
        val fullResponse = StringBuilder()
        var line: String?
        while (true) {
            line = reader.readLine() ?: break
            fullResponse.append(line).append("\n")
            transcript.add("S: $line")
            // SMTP multi-line responses have '-' at index 3 (e.g. "250-SIZE 35882577")
            // The last line has ' ' (e.g. "250 HELP")
            if (line.length >= 4 && line[3] != '-') {
                break
            }
        }
        return fullResponse.toString().trim()
    }
}

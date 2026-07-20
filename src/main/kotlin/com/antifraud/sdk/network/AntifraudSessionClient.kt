package com.antifraud.sdk.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object AntifraudSessionClient {
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    fun createSession(
        apiUrl: String,
        projectId: String,
        timeoutMs: Long,
        encryptedPayload: String
    ): String {
        val client = OkHttpClient.Builder()
            .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .build()

        val jsonBody = JSONObject().apply {
            put("payload", encryptedPayload)
        }.toString()

        var cleanUrl = apiUrl.trim()
        if (cleanUrl.endsWith("/")) {
            cleanUrl = cleanUrl.dropLast(1)
        }
        if (!cleanUrl.startsWith("http://", ignoreCase = true) && !cleanUrl.startsWith("https://", ignoreCase = true)) {
            cleanUrl = "https://$cleanUrl"
        }

        val request = Request.Builder()
            .url("$cleanUrl/v1/session")
            .post(jsonBody.toRequestBody(JSON_MEDIA_TYPE))
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Antifraud-Project-ID", projectId)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optString("error", "HTTP ${response.code}")
                } catch (e: Exception) {
                    "HTTP ${response.code}"
                }
                throw IOException(errorMsg)
            }

            return try {
                val jsonResponse = JSONObject(responseBody)
                jsonResponse.getString("session_id")
            } catch (e: Exception) {
                throw IOException("Invalid response payload: $responseBody")
            }
        }
    }
}

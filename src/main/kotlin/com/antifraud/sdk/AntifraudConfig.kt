package com.antifraud.sdk

data class AntifraudConfig(
    val projectId: String,
    val publicKey: String,
    val apiUrl: String = "https://api.antifraud.id",
    val timeoutMs: Long = 5000L
)

package com.antifraud.sdk.model

import org.json.JSONObject

data class NetworkInfo(
    val ip: String = "",
    val connectionType: String,
    val carrier: String,
    val isVpnActive: Boolean
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("ip", ip)
            put("connectionType", connectionType)
            put("carrier", carrier)
            put("isVpnActive", isVpnActive)
        }
    }
}

data class HardwareInfo(
    val cpuArchitecture: String,
    val totalMemory: Long,
    val freeStorage: Long,
    val screenResolution: String,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val uptime: Long
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("cpuArchitecture", cpuArchitecture)
            put("totalMemory", totalMemory)
            put("freeStorage", freeStorage)
            put("screenResolution", screenResolution)
            put("batteryLevel", batteryLevel)
            put("isCharging", isCharging)
            put("uptime", uptime)
        }
    }
}

data class SecurityInfo(
    val isRootedOrJailbroken: Boolean,
    val isEmulator: Boolean,
    val isMockLocation: Boolean,
    val isDebuggerAttached: Boolean,
    val isAppTampered: Boolean,
    val appSignatureHash: String
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("isRootedOrJailbroken", isRootedOrJailbroken)
            put("isEmulator", isEmulator)
            put("isMockLocation", isMockLocation)
            put("isDebuggerAttached", isDebuggerAttached)
            put("isAppTampered", isAppTampered)
            put("appSignatureHash", appSignatureHash)
        }
    }
}

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("latitude", latitude)
            put("longitude", longitude)
            put("accuracy", accuracy)
        }
    }
}

data class AppInfo(
    val appVersion: String,
    val buildNumber: String
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("appVersion", appVersion)
            put("buildNumber", buildNumber)
        }
    }
}

data class DeviceInfo(
    val deviceId: String,
    val platform: String = "Android",
    val osVersion: String,
    val model: String,
    val manufacturer: String,
    val network: NetworkInfo,
    val hardware: HardwareInfo,
    val security: SecurityInfo,
    val location: LocationInfo,
    val app: AppInfo,
    val collectedAt: String
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("deviceId", deviceId)
            put("platform", platform)
            put("osVersion", osVersion)
            put("model", model)
            put("manufacturer", manufacturer)
            put("network", network.toJsonObject())
            put("hardware", hardware.toJsonObject())
            put("security", security.toJsonObject())
            put("location", location.toJsonObject())
            put("app", app.toJsonObject())
            put("collectedAt", collectedAt)
        }
    }
}

data class MobileSDKPayload(
    val deviceInfo: DeviceInfo
) {
    fun toJsonString(): String {
        val root = JSONObject().apply {
            put("deviceInfo", deviceInfo.toJsonObject())
        }
        return root.toString()
    }
}

package com.antifraud.sdk.model

import org.json.JSONObject

data class NetworkInfo(
    val ip: String = "",
    val isp: String = "",
    val carrier: String,
    val connectionType: String
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("ip", ip)
            put("isp", isp)
            put("carrier", carrier)
            put("connectionType", connectionType)
        }
    }
}

data class SecurityInfo(
    val isRooted: Boolean,
    val isEmulator: Boolean,
    val isDebuggerAttached: Boolean,
    val isAppTampered: Boolean,
    val isMockLocation: Boolean
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("isRooted", isRooted)
            put("isEmulator", isEmulator)
            put("isDebuggerAttached", isDebuggerAttached)
            put("isAppTampered", isAppTampered)
            put("isMockLocation", isMockLocation)
        }
    }
}

data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("level", level)
            put("isCharging", isCharging)
        }
    }
}

data class GpsInfo(
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

data class ScreenInfo(
    val width: Int,
    val height: Int,
    val dpi: Double
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("width", width)
            put("height", height)
            put("dpi", dpi)
        }
    }
}

data class AppInfo(
    val appVersion: String,
    val buildNumber: String
)

data class DeviceInfo(
    val deviceId: String,
    val platform: String,
    val osVersion: String,
    val manufacturer: String,
    val model: String,
    val appVersion: String,
    val buildNumber: String,
    val stableDeviceHash: String,
    val uptime: Long,
    val security: SecurityInfo,
    val battery: BatteryInfo,
    val network: NetworkInfo,
    val gps: GpsInfo,
    val screen: ScreenInfo
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("deviceId", deviceId)
            put("platform", platform)
            put("osVersion", osVersion)
            put("manufacturer", manufacturer)
            put("model", model)
            put("appVersion", appVersion)
            put("buildNumber", buildNumber)
            put("stableDeviceHash", stableDeviceHash)
            put("uptime", uptime)
            put("security", security.toJsonObject())
            put("battery", battery.toJsonObject())
            put("network", network.toJsonObject())
            put("gps", gps.toJsonObject())
            put("screen", screen.toJsonObject())
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

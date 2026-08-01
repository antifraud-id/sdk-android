package com.antifraud.sdk.collector

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import com.antifraud.sdk.model.BatteryInfo
import com.antifraud.sdk.model.ScreenInfo

object DeviceCollector {

    fun getPlatform(): String = "Android"

    fun getOsVersion(): String = Build.VERSION.RELEASE ?: ""

    fun getModel(): String = Build.MODEL ?: ""

    fun getManufacturer(): String = Build.MANUFACTURER ?: ""

    fun getCpuArchitecture(): String =
        if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "unknown"

    fun getTotalMemoryMB(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem / (1024 * 1024)
    }

    fun getBatteryInfo(context: Context): BatteryInfo {
        var batteryLevel = -1
        var isCharging = false
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        if (bm != null) {
            batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val status = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        }
        return BatteryInfo(level = batteryLevel, isCharging = isCharging)
    }

    fun getScreenInfo(context: Context): ScreenInfo {
        val metrics = context.resources.displayMetrics
        val dpi = if (metrics.densityDpi > 0) metrics.densityDpi.toDouble() else 0.0
        return ScreenInfo(
            width = metrics.widthPixels,
            height = metrics.heightPixels,
            dpi = dpi
        )
    }

    fun getUptime(): Long = SystemClock.elapsedRealtime() / 1000
}

package com.antifraud.sdk.collector

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import android.os.StatFs
import com.antifraud.sdk.model.HardwareInfo

object DeviceCollector {

    fun getPlatform(): String = "Android"

    fun getOsVersion(): String = Build.VERSION.RELEASE ?: ""

    fun getModel(): String = Build.MODEL ?: ""

    fun getManufacturer(): String = Build.MANUFACTURER ?: ""

    fun getHardwareInfo(context: Context): HardwareInfo {
        val cpuArch = if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "unknown"

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        val totalMemMB = memoryInfo.totalMem / (1024 * 1024)

        val freeStorageMB = try {
            val stat = StatFs(context.filesDir.absolutePath)
            (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
        } catch (e: Exception) {
            0L
        }

        val metrics = context.resources.displayMetrics
        val screenRes = "${metrics.widthPixels}x${metrics.heightPixels}"

        var batteryLevel = -1
        var isCharging = false
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        if (bm != null) {
            batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val status = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        }

        val uptimeSec = SystemClock.elapsedRealtime() / 1000

        return HardwareInfo(
            cpuArchitecture = cpuArch,
            totalMemory = totalMemMB,
            freeStorage = freeStorageMB,
            screenResolution = screenRes,
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            uptime = uptimeSec
        )
    }
}

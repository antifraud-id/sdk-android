package com.antifraud.sdk

import android.content.Context
import com.antifraud.sdk.collector.AppInfoCollector
import com.antifraud.sdk.collector.DeviceCollector
import com.antifraud.sdk.collector.LocationCollector
import com.antifraud.sdk.collector.NetworkCollector
import com.antifraud.sdk.collector.SecurityCollector
import com.antifraud.sdk.crypto.HybridEncryptor
import com.antifraud.sdk.device.DeviceIdManager
import com.antifraud.sdk.model.DeviceInfo
import com.antifraud.sdk.model.MobileSDKPayload
import com.antifraud.sdk.network.AntifraudSessionClient
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Antifraud {
    const val SDK_VERSION = "android-1.0.0"

    private var config: AntifraudConfig? = null
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    @JvmStatic
    fun initialize(config: AntifraudConfig) {
        this.config = config
    }

    @JvmStatic
    fun initialize(
        projectId: String,
        publicKey: String,
        apiUrl: String? = null,
        timeoutMs: Long? = null
    ) {
        this.config = AntifraudConfig(
            projectId = projectId,
            publicKey = publicKey,
            apiUrl = apiUrl ?: "https://api.antifraud.id",
            timeoutMs = timeoutMs ?: 5000L
        )
    }

    @JvmStatic
    fun createSession(context: Context, callback: (Result<SessionResult>) -> Unit) {
        val currentConfig = config
        if (currentConfig == null) {
            callback(Result.failure(IllegalStateException("Antifraud SDK is not initialized. Call Antifraud.initialize() first.")))
            return
        }

        executor.execute {
            try {
                // 1. Get stable device ID
                val deviceId = DeviceIdManager.getDeviceId(context)

                // 2. Fetch basic info
                val platform = DeviceCollector.getPlatform()
                val osVersion = DeviceCollector.getOsVersion()
                val model = DeviceCollector.getModel()
                val manufacturer = DeviceCollector.getManufacturer()

                val networkInfo = NetworkCollector.getNetworkInfo(context)
                val securityInfo = SecurityCollector.getSecurityInfo(
                    context,
                    mockLocationDetected = isMockLocationDetected(context)
                )
                val batteryInfo = DeviceCollector.getBatteryInfo(context)
                val screenInfo = DeviceCollector.getScreenInfo(context)
                val gpsInfo = LocationCollector.getLocationInfo(context)
                val appInfo = AppInfoCollector.getAppInfo(context)
                val uptime = DeviceCollector.getUptime()

                // 3. Stable device hash — FNV-1a over hardware signals that are
                //    identical across app reinstalls on the same physical device.
                //    The engine uses it to unify identity across SDKs/browsers.
                val stableDeviceHash = fnv1a(listOf(
                    manufacturer,
                    model,
                    osVersion,
                    screenInfo.width.toString(),
                    screenInfo.height.toString(),
                    screenInfo.dpi.toInt().toString(),
                    DeviceCollector.getCpuArchitecture(),
                    DeviceCollector.getTotalMemoryMB(context).toString()
                ).joinToString("|"))

                // 4. Build full payload (matches engine MobileDeviceInfo schema)
                val deviceInfo = DeviceInfo(
                    deviceId = deviceId,
                    platform = platform,
                    osVersion = osVersion,
                    manufacturer = manufacturer,
                    model = model,
                    appVersion = appInfo.appVersion,
                    buildNumber = appInfo.buildNumber,
                    stableDeviceHash = stableDeviceHash,
                    uptime = uptime,
                    security = securityInfo,
                    battery = batteryInfo,
                    network = networkInfo,
                    gps = gpsInfo,
                    screen = screenInfo
                )

                val payload = MobileSDKPayload(deviceInfo)
                val plaintextJSON = payload.toJsonString()

                // 5. Encrypt with hybrid key
                val encryptedData = HybridEncryptor.encryptPayload(currentConfig.publicKey, plaintextJSON)

                // 6. HTTP Post request
                val sessionId = AntifraudSessionClient.createSession(
                    apiUrl = currentConfig.apiUrl,
                    projectId = currentConfig.projectId,
                    timeoutMs = currentConfig.timeoutMs,
                    encryptedPayload = encryptedData
                )

                callback(Result.success(SessionResult(sessionId)))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    private fun isMockLocationDetected(context: Context): Boolean {
        var isMock = false
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            if (lm != null) {
                val providers = lm.getProviders(true)
                for (provider in providers) {
                    val loc = lm.getLastKnownLocation(provider)
                    if (loc != null && loc.isFromMockProvider) {
                        isMock = true
                        break
                    }
                }
            }
        } catch (e: SecurityException) {
            // no permissions
        } catch (e: Exception) {
            // ignore
        }
        return isMock
    }

    private fun fnv1a(input: String): String {
        var hash = 0x811c9dc5.toInt()
        for (b in input.toByteArray(Charsets.UTF_8)) {
            hash = hash xor (b.toInt() and 0xff)
            hash = hash * 16777619
        }
        return String.format(Locale.US, "%08x", hash)
    }
}

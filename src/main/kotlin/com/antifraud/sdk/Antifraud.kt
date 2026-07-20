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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Antifraud {
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
                val deviceId = DeviceIdManager.getDeviceId(context)
                val platform = DeviceCollector.getPlatform()
                val osVersion = DeviceCollector.getOsVersion()
                val model = DeviceCollector.getModel()
                val manufacturer = DeviceCollector.getManufacturer()
                val networkInfo = NetworkCollector.getNetworkInfo(context)
                val hardwareInfo = DeviceCollector.getHardwareInfo(context)
                val locationInfo = LocationCollector.getLocationInfo(context)
                val appInfo = AppInfoCollector.getAppInfo(context)

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

                val securityInfo = SecurityCollector.getSecurityInfo(context, isMock)

                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val collectedAt = sdf.format(Date())

                val deviceInfo = DeviceInfo(
                    deviceId = deviceId,
                    platform = platform,
                    osVersion = osVersion,
                    model = model,
                    manufacturer = manufacturer,
                    network = networkInfo,
                    hardware = hardwareInfo,
                    security = securityInfo,
                    location = locationInfo,
                    app = appInfo,
                    collectedAt = collectedAt
                )

                val payload = MobileSDKPayload(deviceInfo)
                val plaintextJSON = payload.toJsonString()

                val encryptedData = HybridEncryptor.encryptPayload(currentConfig.publicKey, plaintextJSON)

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
}

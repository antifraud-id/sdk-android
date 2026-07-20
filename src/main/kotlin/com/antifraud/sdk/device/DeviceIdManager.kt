package com.antifraud.sdk.device

import android.content.Context
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.util.UUID

object DeviceIdManager {
    private const val PREFS_FILE = "antifraud_secure_prefs"
    private const val KEY_DEVICE_ID = "device_id"

    @Synchronized
    fun getDeviceId(context: Context): String {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val sharedPreferences = EncryptedSharedPreferences.create(
                PREFS_FILE,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            var deviceId = sharedPreferences.getString(KEY_DEVICE_ID, null)
            if (deviceId.isNullOrEmpty()) {
                deviceId = getFallbackDeviceId(context)
                sharedPreferences.edit().putString(KEY_DEVICE_ID, deviceId).apply()
            }
            return deviceId
        } catch (e: Exception) {
            return try {
                val backupPrefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
                var deviceId = backupPrefs.getString(KEY_DEVICE_ID, null)
                if (deviceId.isNullOrEmpty()) {
                    deviceId = getFallbackDeviceId(context)
                    backupPrefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
                }
                deviceId
            } catch (ex: Exception) {
                getFallbackDeviceId(context)
            }
        }
    }

    private fun getFallbackDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return if (!androidId.isNullOrEmpty() && androidId != "9774d56d682e549c") {
            try {
                UUID.nameUUIDFromBytes(androidId.toByteArray(Charsets.UTF_8)).toString()
            } catch (e: Exception) {
                UUID.randomUUID().toString()
            }
        } else {
            UUID.randomUUID().toString()
        }
    }
}

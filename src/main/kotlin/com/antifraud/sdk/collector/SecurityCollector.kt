package com.antifraud.sdk.collector

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import com.antifraud.sdk.model.SecurityInfo
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest

object SecurityCollector {

    fun getSecurityInfo(context: Context, mockLocationDetected: Boolean): SecurityInfo {
        val rooted = isRooted(context)
        val emulator = isEmulator()
        val debugger = isDebuggerAttached(context)
        val tampered = isAppTampered(context)
        val sigHash = getAppSignatureHash(context)

        return SecurityInfo(
            isRootedOrJailbroken = rooted,
            isEmulator = emulator,
            isMockLocation = mockLocationDetected,
            isDebuggerAttached = debugger,
            isAppTampered = tampered,
            appSignatureHash = sigHash
        )
    }

    private fun isRooted(context: Context): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }

        val tags = Build.TAGS
        if (tags != null && tags.contains("test-keys")) {
            return true
        }

        val rootPackages = arrayOf(
            "com.topjohnwu.magisk",
            "eu.chainfire.supersu",
            "com.noshufou.android.su",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser"
        )
        val pm = context.packageManager
        for (pkg in rootPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // not found
            }
        }

        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                val line = reader.readLine()
                if (line != null && line.isNotEmpty()) {
                    return true
                }
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            process?.destroy()
        }

        return false
    }

    private fun isEmulator(): Boolean {
        val finger = Build.FINGERPRINT ?: ""
        val model = Build.MODEL ?: ""
        val manuf = Build.MANUFACTURER ?: ""
        val brand = Build.BRAND ?: ""
        val device = Build.DEVICE ?: ""
        val product = Build.PRODUCT ?: ""
        val hardware = Build.HARDWARE ?: ""

        val isGeneric = finger.contains("generic") ||
                finger.contains("vbox86p") ||
                model.contains("google_sdk") ||
                model.contains("Emulator") ||
                model.contains("Android SDK") ||
                manuf.contains("Genymotion") ||
                (brand == "generic" && device == "generic") ||
                product.contains("sdk") ||
                product.contains("vbox86p") ||
                hardware.contains("goldfish") ||
                hardware.contains("ranchu")

        if (isGeneric) return true

        val qemuFiles = arrayOf("/dev/socket/qemud", "/dev/qemu_pipe")
        for (file in qemuFiles) {
            if (File(file).exists()) return true
        }

        return false
    }

    private fun isDebuggerAttached(context: Context): Boolean {
        if (Debug.isDebuggerConnected()) return true

        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) return true

        try {
            val file = File("/proc/self/status")
            if (file.exists()) {
                file.forEachLine { line ->
                    if (line.startsWith("TracerPid:")) {
                        val pid = line.substringAfter("TracerPid:").trim().toIntOrNull()
                        if (pid != null && pid != 0) {
                            return true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }

        try {
            val mapsFile = File("/proc/self/maps")
            if (mapsFile.exists()) {
                mapsFile.forEachLine { line ->
                    if (line.contains("frida", ignoreCase = true)) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }

        return false
    }

    private fun isAppTampered(context: Context): Boolean {
        try {
            Class.forName("de.robv.android.xposed.XposedBridge")
            return true
        } catch (e: ClassNotFoundException) {
            // ignore
        }

        return false
    }

    private fun getAppSignatureHash(context: Context): String {
        return try {
            val pm = context.packageManager
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                packageInfo.signatures
            }

            if (!signatures.isNullOrEmpty()) {
                val sig = signatures[0]
                val md = MessageDigest.getInstance("SHA-256")
                md.update(sig.toByteArray())
                val digest = md.digest()
                digest.joinToString("") { "%02x".format(it) }
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}

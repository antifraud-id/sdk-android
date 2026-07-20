package com.antifraud.sdk.collector

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.antifraud.sdk.model.AppInfo

object AppInfoCollector {

    fun getAppInfo(context: Context): AppInfo {
        var version = ""
        var build = ""
        try {
            val pm = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, 0)
            }
            version = packageInfo.versionName ?: ""
            build = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
        } catch (e: Exception) {
            // ignore
        }
        return AppInfo(appVersion = version, buildNumber = build)
    }
}

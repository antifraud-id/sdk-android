package com.antifraud.sdk.collector

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.antifraud.sdk.model.GpsInfo

object LocationCollector {

    fun getLocationInfo(context: Context): GpsInfo {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return GpsInfo(0.0, 0.0, 0.0)
        }

        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (lm == null) {
                return GpsInfo(0.0, 0.0, 0.0)
            }

            var bestLocation: Location? = null
            val providers = lm.getProviders(true)
            for (provider in providers) {
                val loc = lm.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }

            if (bestLocation != null) {
                GpsInfo(
                    latitude = bestLocation.latitude,
                    longitude = bestLocation.longitude,
                    accuracy = bestLocation.accuracy.toDouble()
                )
            } else {
                GpsInfo(0.0, 0.0, 0.0)
            }
        } catch (e: SecurityException) {
            GpsInfo(0.0, 0.0, 0.0)
        } catch (e: Exception) {
            GpsInfo(0.0, 0.0, 0.0)
        }
    }
}

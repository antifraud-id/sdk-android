package com.antifraud.sdk.collector

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.antifraud.sdk.model.LocationInfo

object LocationCollector {

    fun getLocationInfo(context: Context): LocationInfo {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return LocationInfo(0.0, 0.0, 0.0)
        }

        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (lm == null) {
                return LocationInfo(0.0, 0.0, 0.0)
            }

            var bestLocation: android.location.Location? = null
            val providers = lm.getProviders(true)
            for (provider in providers) {
                val loc = lm.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }

            if (bestLocation != null) {
                LocationInfo(
                    latitude = bestLocation.latitude,
                    longitude = bestLocation.longitude,
                    accuracy = bestLocation.accuracy.toDouble()
                )
            } else {
                LocationInfo(0.0, 0.0, 0.0)
            }
        } catch (e: SecurityException) {
            LocationInfo(0.0, 0.0, 0.0)
        } catch (e: Exception) {
            LocationInfo(0.0, 0.0, 0.0)
        }
    }
}

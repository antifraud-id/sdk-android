# Antifraud Android SDK

[![](https://jitpack.io/v/antifraud-id/sdk-android.svg)](https://jitpack.io/#antifraud-id/sdk-android)

A native fraud detection and device fingerprinting SDK for Android applications. The SDK collects hardware specifications, OS attributes, carrier metadata, location signals, and deep security diagnostics (root/jailbreak, emulator, debugger, mock location, and tampering hooks), encrypts the payload using hybrid RSA-OAEP + AES-GCM cryptography, and exchanges it for a stable `session_id` via the Antifraud API.

## Features

* **Stable Device Identity**: UUID stored in `EncryptedSharedPreferences` to survive application uninstalls.
* **Network Diagnostics**: Cellular carrier name, active VPN detection, and connection type (WIFI/4G/5G/etc).
* **Hardware Profile**: CPU architecture, physical memory size, available storage space, screen resolution, and battery capacity/charging state.
* **Security & Tamper Protections**:
  * Multi-layer Root binary, path, and package detectors.
  * Emulator detection.
  * Active debugger attachment checks.
  * Code hooks and framework injection detection (Xposed).
  * Application binary signature verification.
* **Location Harvest**: Coordinates and horizontal accuracy (graceful degradation when permissions are absent).

## Installation

### Gradle (Maven Central)

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.antifraud.sdk:antifraud-sdk:1.0.0")
}
```

### JitPack

Add the JitPack repository to your `settings.gradle.kts`:

```kotlin
dependencyResolution {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Then add the dependency:

```kotlin
dependencies {
    implementation("com.antifraud-id:sdk-android:1.0.0")
}
```

## Permissions

Add the following permissions to your `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Required for API communication -->
    <uses-permission android:name="android.permission.INTERNET" />
    
    <!-- Optional: For location signal profiling -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
</manifest>
```

## Usage

### 1. Initialize the SDK

Initialize the SDK once, ideally in your `Application` class or `MainActivity`:

```kotlin
import com.antifraud.sdk.Antifraud
import com.antifraud.sdk.AntifraudConfig

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        Antifraud.initialize(
            AntifraudConfig(
                projectId = "your-project-uuid-here",
                publicKey = """-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv62K/N9g5P8i...
-----END PUBLIC KEY-----""",
                apiUrl = "https://api.antifraud.id",  // Optional
                timeoutMs = 5000                       // Optional
            )
        )
    }
}
```

### 2. Generate a Session ID

Call `createSession()` before sensitive user events like registrations, logins, or checkouts. Always implement a **fail-open strategy**:

```kotlin
import com.antifraud.sdk.Antifraud

fun handleCheckout(amount: Double) {
    var sessionId: String? = null
    
    Antifraud.createSession(this) { result ->
        result.fold(
            onSuccess = { sessionResult ->
                sessionId = sessionResult.sessionId
            },
            onFailure = { exception ->
                // Fail-open: log error but allow flow to continue
                Log.e("Antifraud", "Session generation failed", exception)
            }
        )
        
        // Forward sessionId (may be null) to your backend
        myBackendApi.processOrder(amount, sessionId)
    }
}
```

## ProGuard / R8

The SDK includes consumer ProGuard rules. No additional configuration is needed.

## License

```
Copyright 2024 Antifraud.id

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

# Antifraud Android SDK

A native fraud detection and device fingerprinting SDK for Android applications.

## Features

* **Stable Device Identity**: UUID stored in `EncryptedSharedPreferences`
* **Network Diagnostics**: Carrier name, VPN detection, connection type
* **Hardware Profile**: CPU, memory, storage, screen, battery
* **Security Detection**: Root, emulator, debugger, mock location, tampering
* **Location Harvest**: Coordinates with graceful permission degradation

## Installation

### Gradle

```kotlin
dependencies {
    implementation("com.antifraud.sdk:antifraud-sdk:1.0.0")
}
```

## Usage

```kotlin
// Initialize
Antifraud.initialize(
    AntifraudConfig(
        projectId = "your-project-id",
        publicKey = "your-public-key"
    )
)

// Create session
Antifraud.createSession(context) { result ->
    result.fold(
        onSuccess = { session ->
            // Use session.sessionId
        },
        onFailure = { error ->
            // Handle error
        }
    )
}
```

## License

Apache License 2.0

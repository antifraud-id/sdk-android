pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            when {
                requested.id.id.startsWith("com.android") -> useModule("com.android.tools.build:gradle:8.9.2")
                requested.id.id.startsWith("org.jetbrains.kotlin") -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "antifraud-sdk-android"

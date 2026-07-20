plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.antifraud.sdk"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}

afterEvaluate {
    publishing {
        repositories {
            maven {
                name = "mavenCentral"
                val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

                credentials {
                    username = project.findProperty("ossrhUsername") as? String ?: System.getenv("OSSRH_USERNAME")
                    password = project.findProperty("ossrhPassword") as? String ?: System.getenv("OSSRH_PASSWORD")
                }
            }
        }

        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.antifraud.sdk"
                artifactId = "antifraud-sdk"
                version = "1.0.0"

                pom {
                    name.set("Antifraud SDK")
                    description.set("Android SDK for Antifraud.id integration - fraud detection and device fingerprinting")
                    url.set("https://github.com/antifraud-id/sdk-android")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("antifraud")
                            name.set("Antifraud.id")
                            email.set("info@antifraud.id")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/antifraud-id/sdk-android.git")
                        developerConnection.set("scm:git:ssh://github.com/antifraud-id/sdk-android.git")
                        url.set("https://github.com/antifraud-id/sdk-android")
                    }
                }
            }
        }
    }

    signing {
        val signingKeyId = project.findProperty("signing.keyId") as? String ?: System.getenv("GPG_KEY_ID")
        val signingPassword = project.findProperty("signing.password") as? String ?: System.getenv("GPG_PASSWORD")
        val signingKey = project.findProperty("signing.key") as? String ?: System.getenv("GPG_KEY")

        if (signingKeyId != null && signingPassword != null && signingKey != null) {
            useInMemoryPgpKeys(signingKeyId, signingPassword, signingKey)
        }

        sign(publishing.publications["release"])
    }
}

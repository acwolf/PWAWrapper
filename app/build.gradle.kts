import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.acwolf.pwawrapper"
    compileSdk = 35

    // 1. Load your local.properties safely
    val localProperties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }


    val originalFallback = "\"https://developer.mozilla.org/en-US/docs/Learn_web_development/Howto/Tools_and_setup/Checking_that_your_web_site_is_working_properly\""

    val appUrl = localProperties.getProperty("app.url") ?: originalFallback
    val loginUrl = localProperties.getProperty("app.login") ?: originalFallback

    defaultConfig {
        applicationId = "com.acwolf.pwawrapper"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val appUrl = localProperties.getProperty("app.url") ?: "\"https://www.expensecaptain.com/\""
        val loginUrl = localProperties.getProperty("app.login") ?: "\"https://www.expensecaptain.com/api/login.php\""

        // This injects the variables into the generated BuildConfig class
        buildConfigField("String", "APP_URL", appUrl)
        buildConfigField("String", "LOGIN_URL", loginUrl)

        // Inject variables into Kotlin
        buildConfigField("String", "WEB_URL", appUrl)
        buildConfigField("String", "LOGIN_URL", loginUrl)
    }

    buildFeatures {
        buildConfig = true
    }

    // 3. THE FIX: Synchronize Java and Kotlin to JVM 21
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    // Alternative "Pro" way to ensure alignment
    kotlin {
        jvmToolchain(21)
    }
}

dependencies {
    // UI and Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // PWA & Security Features
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Coroutines and Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
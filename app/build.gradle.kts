import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.acwolf.pwawrapper"
    compileSdk = 35

    // 1. Load local.properties for your private URL
    val properties = Properties().apply {
        val propertiesFile = project.rootProject.file("local.properties")
        if (propertiesFile.exists()) {
            load(propertiesFile.inputStream())
        }
    }

    // 2. Prepare the URL for code injection
    val apiUrl = properties.getProperty("api.url") ?: "\"https://fallback-api.com\""

    defaultConfig {
        applicationId = "com.acwolf.pwawrapper"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 3. This creates BuildConfig.BASE_URL
        buildConfigField("String", "BASE_URL", apiUrl)
    }

    buildFeatures {
        buildConfig = true
    }

    // ... (rest of your build types and compile options) ...
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
}
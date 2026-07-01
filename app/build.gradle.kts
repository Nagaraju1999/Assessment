import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.nagaraju.stocktracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nagaraju.stocktracker"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read the Finnhub API key from local.properties using the standard
        // java.util.Properties approach — reliable across all AGP versions,
        // unlike the internal gradleLocalProperties() API.
        val localProperties = Properties().apply {
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                load(localPropertiesFile.inputStream())
            }
        }
        val finnhubApiKey = localProperties.getProperty("FINNHUB_API_KEY", "")
        buildConfigField("String", "FINNHUB_API_KEY", "\"$finnhubApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Feature modules — app wires them together via Navigation
    implementation(project(":feature-watchlist"))
    implementation(project(":feature-stock-details"))
    implementation(project(":feature-alerts"))

    // Core UI contains the app theme
    implementation(project(":core-ui"))

    // Data module provides Hilt bindings (repository implementations)
    implementation(project(":data"))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM — all compose-* versions are managed by this BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt — DI framework; @HiltAndroidApp on Application class
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Debug only
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Instrumented tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

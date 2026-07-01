plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.nagaraju.stocktracker.core.common"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Result lives in :domain (pure Kotlin) — core-common's FlowExtensions
    // wraps Flow emissions in Result, so it depends on domain for that type.
    // This is the one sanctioned exception to the usual dependency direction:
    // Result is a cross-cutting type with zero Android coupling, so placing
    // it in domain (the architectural root) and letting this Android-aware
    // utility module depend on it keeps domain itself dependency-free.
    implementation(project(":domain"))

    // Coroutines — DispatcherProvider abstracts Dispatchers for testability
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Core KTX — used by NetworkMonitor (ConnectivityManager)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

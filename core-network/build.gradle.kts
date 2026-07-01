plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.nagaraju.stocktracker.core.network"
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
    implementation(project(":core-common"))

    // Retrofit — declarative HTTP API definitions
    implementation(libs.retrofit.core)
    // Gson converter — deserializes JSON responses into DTOs
    implementation(libs.retrofit.converter.gson)

    // OkHttp — the HTTP engine Retrofit runs on
    implementation(libs.okhttp.core)
    // Logs request/response bodies in Logcat during debug builds
    implementation(libs.okhttp.logging.interceptor)

    // Coroutines — suspend functions in Retrofit service interfaces
    implementation(libs.kotlinx.coroutines.core)

    // Hilt — @Module/@Provides for NetworkModule, @Inject for ApiRateLimiter/StockRemoteSource callers
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}

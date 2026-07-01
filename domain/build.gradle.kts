// Domain is pure Kotlin — no Android SDK dependency.
// This enforces the Clean Architecture rule: business logic has zero platform coupling.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // javax.inject — provides the @Inject annotation used on every use case
    // constructor. Hilt (configured in :app and feature modules) generates
    // the actual injection code; domain only needs the annotation itself.
    implementation(libs.javax.inject)

    // Coroutines — Flow return types on repository interfaces and use case outputs
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

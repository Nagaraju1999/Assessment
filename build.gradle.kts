// Top-level build file. Plugin declarations only — no dependencies here.
// Each module's own build.gradle.kts applies only the plugins it needs.
plugins {
    alias(libs.plugins.android.application)    apply false
    alias(libs.plugins.android.library)        apply false
    alias(libs.plugins.kotlin.android)         apply false
    alias(libs.plugins.kotlin.compose)         apply false
    alias(libs.plugins.kotlin.jvm)             apply false
    alias(libs.plugins.hilt.android)           apply false
    alias(libs.plugins.ksp)                    apply false
}

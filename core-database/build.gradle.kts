plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    // KSP (Kotlin Symbol Processing) generates Room DAOs and type converters at compile time
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.nagaraju.stocktracker.core.database"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        // Room exports its schema as JSON here on every build — useful for
        // tracking schema history and writing migration tests later.
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests {
            // Required for Robolectric to load Android framework resources (manifest, etc.)
            isIncludeAndroidResources = true
        }
    }

    // Exposes the KSP-generated schema JSON files to MigrationTestHelper at
    // test runtime, so DatabaseMigrationTest can validate real schema diffs
    // rather than only exercising the migration SQL in isolation.
    sourceSets {
        getByName("test") {
            assets.srcDirs("$projectDir/schemas")
        }
    }
}

dependencies {
    implementation(project(":core-common"))

    // Room runtime — @Database, @Entity, @Dao
    implementation(libs.room.runtime)
    // Room Kotlin extensions — provides suspend functions and Flow support on DAOs
    implementation(libs.room.ktx)
    // KSP processor — generates Room implementation classes at build time
    ksp(libs.room.compiler)

    // Coroutines — Flow return types in DAOs
    implementation(libs.kotlinx.coroutines.core)

    // Hilt — @Module/@Provides for DatabaseModule
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.room.testing)
}

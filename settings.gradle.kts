pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "StockMarketTracker"

// All modules declared here — Gradle will resolve each as a subproject
include(":app")
include(":core-common")
include(":core-network")
include(":core-database")
include(":core-ui")
include(":domain")
include(":data")
include(":feature-watchlist")
include(":feature-stock-details")
include(":feature-alerts")

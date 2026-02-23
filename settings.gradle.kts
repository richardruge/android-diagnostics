pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com.android.*")
                includeGroupByRegex("com.google.*")
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
        maven { url = uri("https://jitpack.io") }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "AndroidDiagnostics"

include(":app")
include(":core-model")
include(":core-data")
include(":core-system")
include(":feature-battery")
include(":feature-network")
include(":feature-thermal")
include(":feature-automation")

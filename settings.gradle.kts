rootProject.name = "AdventureLog"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":composeApp")

include(":core:common")
include(":core:data")
include(":core:domain")
include(":core:model")
include(":core:network")
include(":core:permissions")
include(":core:testing")

include(":feature:login")
include(":feature:locations")
include(":feature:calendar")
include(":feature:collections")
include(":feature:home")
include(":feature:settings")
include(":feature:detail")
include(":feature:ui")
include(":feature:world")
include(":feature:map")

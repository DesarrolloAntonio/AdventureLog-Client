plugins {
    alias(libs.plugins.adventurelog.kotlinMultiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildConfig)
}

kotlin {
    sourceSets {

        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }
        commonMain.dependencies {
            implementation(projects.core.model)

            implementation(libs.kotlinx.serialization.json)
            api(libs.bundles.ktor.common)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Opt-in flag for verbose HTTP logging. Off by default so release builds never print request or
// response bodies; enable locally with -Padventurelog.httpLogging=true. Even when on, credentials
// and session material are redacted (see HttpLogging.kt).
val httpLoggingEnabled = providers.gradleProperty("adventurelog.httpLogging")
    .map { it.toBoolean() }
    .getOrElse(false)

buildConfig {
    // Its own package, not the app's. Generating into com.desarrollodroide.adventurelog put this
    // class at the same fully-qualified name as the one AGP generates for composeApp, and the two
    // collided when dexing - which is why assembleRelease had never once succeeded.
    packageName = "com.desarrollodroide.adventurelog.core.network"
    useKotlinOutput { internalVisibility = true }
    buildConfigField(
        "String",
        "APP_NAME",
        "\"${rootProject.name}\""
    )
    buildConfigField(
        "Boolean",
        "HTTP_LOGGING",
        "$httpLoggingEnabled"
    )
}
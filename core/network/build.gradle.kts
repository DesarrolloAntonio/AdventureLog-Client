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
    }
}

buildConfig {
    packageName = "com.desarrollodroide.adventurelog"
    useKotlinOutput { internalVisibility = true }
    buildConfigField(
        "String",
        "APP_NAME",
        "\"${rootProject.name}\""
    )
}
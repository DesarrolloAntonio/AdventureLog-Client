import java.util.Properties

plugins {
    alias(libs.plugins.adventurelog.kotlinMultiplatform)
    alias(libs.plugins.adventurelog.composeMultiplatform)
    alias(libs.plugins.buildConfig)
}

// Shared with composeApp, which needs the same key for the interactive map. Empty when absent -
// the placeholder simply skips the map layer and shows its coordinate artwork alone.
val mapsApiKey: String = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}.getProperty("MAPS_API_KEY", "")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            implementation(projects.core.model)
            api(projects.core.domain)

            implementation(libs.koin.composeVM)

            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.androidx.ui.tooling)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

buildConfig {
    packageName = "com.desarrollodroide.adventurelog.feature.ui"
    useKotlinOutput { internalVisibility = true }
    buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
}

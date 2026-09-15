@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

plugins {
    alias(libs.plugins.adventurelog.kotlinMultiplatform)
    alias(libs.plugins.adventurelog.composeMultiplatform)
}

android {
    namespace = "com.desarrollodroide.adventurelog.feature.home"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            implementation(projects.core.model)
            api(projects.core.domain)
            implementation(projects.feature.settings)
            implementation(projects.feature.locations)
            implementation(projects.feature.ui)
            implementation(projects.feature.calendar)
            implementation(projects.feature.collections)
            implementation(projects.feature.world)
            implementation(projects.feature.map)
            implementation(projects.feature.detail)

            implementation(libs.koin.composeVM)
            implementation(libs.material3.adaptive.navigation.suite)
            implementation(libs.navigation3.ui)
            implementation(libs.navigation3.runtime)
            implementation(libs.material3.adaptive.navigation3)

            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.androidx.ui.tooling)
        }
        // Screen tests run on a device: this project has no JVM target, so there is nowhere
        // else for a Compose test to execute.
        androidInstrumentedTest.dependencies {
            // compose.uiTest, not the androidx artifact by name: Compose Multiplatform resolves
            // the version, and naming androidx.compose.ui:ui-test-junit4-android directly asks
            // for a version nothing here declares.
            implementation(compose.uiTest)
            implementation(libs.androidx.test.runner)
            implementation(libs.junit)
            // compose.uiTest drags in Espresso 3.5.0, whose input injection calls
            // InputManager.getInstance - gone in Android 17, so every test dies before it runs.
            implementation(libs.androidx.espresso.core)
            // Declares the ComponentActivity the test host launches into.
            implementation(libs.compose.ui.test.manifest)
        }
    }
}
@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

plugins {
    alias(libs.plugins.adventurelog.kotlinMultiplatform)
    alias(libs.plugins.adventurelog.composeMultiplatform)
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            // The repository stub has to name PagingData to override two of its methods.
            implementation(libs.multiplatform.paging.common)
        }
        commonMain.dependencies {
            api(projects.core.common)
            implementation(projects.core.model)
            api(projects.core.domain)
            implementation(projects.feature.ui)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel)
        }
        androidInstrumentedTest.dependencies {
            implementation(projects.core.testing)
            implementation(projects.core.model)
            implementation(compose.uiTest)
            implementation(libs.androidx.test.runner)
            implementation(libs.junit)
            // compose.uiTest drags in Espresso 3.5.0, whose input injection calls
            // InputManager.getInstance - gone in Android 17, so every test dies before it runs.
            implementation(libs.androidx.espresso.core)
            implementation(libs.compose.ui.test.manifest)
        }
        androidUnitTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.koin.composeVM)
            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.coil.compose)
            implementation(libs.androidx.ui.tooling)
            
            // Google Maps for Jetpack Compose
        }
    }
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

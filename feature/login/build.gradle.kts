@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

plugins {
    alias(libs.plugins.adventurelog.kotlinMultiplatform)
    alias(libs.plugins.adventurelog.composeMultiplatform)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            implementation(projects.core.model)
            api(projects.core.domain)
            implementation(projects.feature.home)
            implementation(projects.feature.ui)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.savedstate)
        }
        androidMain.dependencies {
            implementation(libs.koin.composeVM)
            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.coil.compose)
            implementation(libs.androidx.ui.tooling)
            // Custom Tabs: a provider sign-in runs in the system browser, never in a WebView.
            implementation(libs.androidx.browser)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

kotlin {
    sourceSets {
        androidInstrumentedTest.dependencies {
            implementation(compose.uiTest)
            implementation(libs.androidx.test.runner)
            implementation(libs.junit)
            implementation(libs.androidx.espresso.core)
            implementation(libs.compose.ui.test.manifest)
        }
    }
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

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
            implementation(projects.feature.detail)
            implementation(projects.feature.ui)
            implementation(libs.koin.composeVM)
            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.multiplatform.paging.compose)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.androidx.ui.tooling)
        }
    }
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
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

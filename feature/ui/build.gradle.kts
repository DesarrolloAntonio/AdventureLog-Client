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
            // The image client and the file downloader are plain Ktor.
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.koin.composeVM)
            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.androidx.ui.tooling)
            // BackHandler, for the discard guard on forms.
            implementation(libs.androidx.activity.compose)
            // The app's map (AppMap) and its MapLibre backend. Nothing from Google here: the Google
            // backend lives in :feature:map-google, which only the play flavor ships.
            implementation(libs.maplibre.compose)
            implementation(libs.maplibre.compose.runtime.opengl)
            // Raster basemaps are written as MapLibre style documents.
            implementation(libs.kotlinx.serialization.json)
            // The map engine setting is kept on the phone, in the same store as the theme. api: the
            // app builds MapEngineStore from the Settings it already has.
            api(libs.multiplatform.settings)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidUnitTest.dependencies {
            implementation(libs.multiplatform.settings.test)
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

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
            // BackHandler, for the discard guard on forms.
            implementation(libs.androidx.activity.compose)
            // api, not implementation: toGoogleMapType() returns a MapType, so the type has to
            // stay on the compile classpath of the feature modules that call it.
            api(libs.maps.compose)
            api(libs.play.services.maps)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
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

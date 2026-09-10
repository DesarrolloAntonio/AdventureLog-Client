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
            implementation(libs.multiplatform.paging.compose)
        }
        commonMain.dependencies {
            api(projects.core.common)
            implementation(projects.core.model)
            api(projects.core.domain)
            implementation(projects.feature.ui)

            implementation(libs.koin.composeVM)

            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)

        }
        androidMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.androidx.ui.tooling)
            
            // Google Maps for Jetpack Compose
            implementation(libs.maps.compose)
            implementation(libs.play.services.maps)
        }
    }
}

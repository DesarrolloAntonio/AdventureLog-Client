plugins {
    alias(libs.plugins.adventurelog.kotlinMultiplatform)
    alias(libs.plugins.adventurelog.composeMultiplatform)
}

// The Google Maps engine for AppMap. Only the play flavor of composeApp depends on this module, so
// the fdroid build carries no Google Play Services at all. Android code only: the iOS targets the
// convention adds stay empty.
kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(projects.feature.ui)
            implementation(projects.core.model)
            implementation(libs.maps.compose)
            implementation(libs.maps.compose.utils)
            implementation(libs.play.services.maps)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

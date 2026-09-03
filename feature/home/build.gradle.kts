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
            implementation(projects.feature.settings)
            implementation(projects.feature.locations)
            implementation(projects.feature.ui)
            implementation(projects.feature.calendar)
            implementation(projects.feature.collections)
            implementation(projects.feature.world)
            implementation(projects.feature.map)

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
        androidMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.androidx.ui.tooling)
        }
    }
}
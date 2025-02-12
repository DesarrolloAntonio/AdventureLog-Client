plugins {
    alias(libs.plugins.adventurelog.kotlinMultiplatform)
    id("com.google.protobuf") version "0.9.4"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            implementation(projects.core.model)
            api(projects.core.network)
            implementation("com.russhwolf:multiplatform-settings:1.3.0")
            implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")
        }
    }
}

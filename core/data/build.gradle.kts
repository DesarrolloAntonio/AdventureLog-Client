plugins {
    alias(libs.plugins.adventurelog.kotlinMultiplatform)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            implementation(projects.core.model)
            api(projects.core.network)
        }
    }
}

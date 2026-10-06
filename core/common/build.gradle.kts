plugins {
    alias(libs.plugins.adventurelog.kotlinMultiplatform)
    alias(libs.plugins.adventurelog.composeMultiplatform)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.desarrollodroide.adventurelog.resources"
    generateResClass = always
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

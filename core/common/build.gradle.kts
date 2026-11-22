plugins {
    alias(libs.plugins.adventurelog.kotlinMultiplatform)
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

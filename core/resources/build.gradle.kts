import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// The app's Compose resources (fonts, strings) and their Res class. Android only: Compose
// resources generate Res into commonMain and need the Compose runtime there, so a module with an
// iOS target would put Compose back into the iOS framework. Consumed from androidMain only.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(17)

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            api(compose.components.resources)
        }
    }
}

android {
    namespace = "com.desarrollodroide.adventurelog.core.resources"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.desarrollodroide.adventurelog.resources"
    generateResClass = always
}

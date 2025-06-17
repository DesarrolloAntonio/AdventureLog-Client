@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.composeHotReload)
}

// Load API keys from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val mapsApiKey = localProperties.getProperty("MAPS_API_KEY", "")

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            optimized = true
        }
    }
    
    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.activity.ktx)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.android)
            implementation(libs.kotlinx.coroutines.android)
        }

        commonMain.dependencies {
            implementation(projects.feature.login)
            implementation(projects.feature.home)
            implementation(projects.feature.adventures)
            implementation(projects.feature.settings)
            implementation(projects.feature.detail)
            implementation(projects.feature.ui)
            implementation(projects.feature.collections)
            implementation(projects.core.model)

            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.navigation.compose)

            api(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.ktor.client.core)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            implementation(libs.filekit.core)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.desarrollodroide.adventurelog"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.desarrollodroide.adventurelog"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.0.1-alpha"

        // Set the Maps API Key as a BuildConfig field
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            resValue("string", "app_name", "AdventureLog (Dev)")
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

// Hot reload support
composeCompiler {
    featureFlags = setOf(
        ComposeFeatureFlag.StrongSkipping,
        ComposeFeatureFlag.OptimizeNonSkippingGroups
    )
}
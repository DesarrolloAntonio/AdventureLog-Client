@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.skie)
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
            // Every module whose types Swift names. Each one is an api dependency below.
            export(projects.core.model)
            export(projects.core.domain)
            export(projects.feature.login)
            export(projects.feature.home)
            export(projects.feature.locations)
            export(projects.feature.settings)
            export(projects.feature.detail)
            export(projects.feature.ui)
            export(projects.feature.calendar)
            export(projects.feature.collections)
            export(projects.feature.world)
            export(projects.feature.map)
            optimized = true
        }
    }
    
    sourceSets {

        androidMain.dependencies {
            // The Compose UI is Android only; iOS draws SwiftUI over the same ViewModels.
            implementation(projects.core.resources)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(libs.androidx.compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.navigation.compose)
            implementation(libs.koin.compose)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.activity.ktx)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.android)
            implementation(libs.kotlinx.coroutines.android)
        }

        commonMain.dependencies {
            api(projects.feature.login)
            api(projects.feature.home)
            api(projects.feature.locations)
            api(projects.feature.settings)
            api(projects.feature.detail)
            api(projects.feature.ui)
            api(projects.feature.calendar)
            api(projects.feature.collections)
            api(projects.feature.world)
            api(projects.feature.map)
            api(projects.core.model)
            api(projects.core.domain)
            implementation(projects.core.data)

            api(libs.koin.core)

            implementation(libs.ktor.client.core)

            implementation(libs.filekit.core)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            // The SwiftUI bridge: a ViewModel owner with saved state, and a presenter for PagingData.
            implementation(libs.androidx.lifecycle.viewmodel.savedstate)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.multiplatform.paging.common)
        }

        iosTest.dependencies {
            implementation(libs.kotlin.test)
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
        versionCode = (findProperty("versionCode") as String).toInt()
        versionName = findProperty("versionName") as String

        // Set the Maps API Key as a BuildConfig field
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // The keystore never lives in this repository. CI writes it from a secret and passes the
    // credentials as environment variables; a machine without them simply builds unsigned, so a
    // clone still compiles for anyone who is not publishing.
    val keystorePath: String? = System.getenv("KEYSTORE_PATH")

    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("RELEASE_STORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            resValue("string", "app_name", "AdventureLog (Dev)")
        }
        getByName("release") {
            // Play flagged both consequences of leaving this off: no mapping file for crash
            // reports, and a download larger than it needs to be.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
        // AGP 9 turns this off by default; the debug build type sets app_name through it.
        resValues = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

composeCompiler {
    // Compose is compiled for Android only (see ComposeMultiplatformConventionPlugin).
    targetKotlinPlatforms.set(setOf(KotlinPlatformType.androidJvm))
    featureFlags = setOf(
        ComposeFeatureFlag.OptimizeNonSkippingGroups
    )
}
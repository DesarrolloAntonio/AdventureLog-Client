@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

plugins {
    alias(libs.plugins.adventurelog.kotlinMultiplatform)
    alias(libs.plugins.adventurelog.composeMultiplatform)
    alias(libs.plugins.androidLibrary)
}

/**
 * Android configuration block required to process native Android resources.
 * This configuration enables the module to compile and access Android XML resources
 * such as styles.xml, which is specifically needed for theming the native 
 * EmojiPickerView component used in the emoji selection feature.
 * 
 * Without this block, the module cannot generate the R class necessary to reference
 * XML resources from Kotlin code, even though the module already has androidMain
 * source sets configured through Kotlin Multiplatform.
 */
android {
    namespace = "com.desarrollodroide.adventurelog.feature.locations"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidInstrumentedTest.dependencies {
            implementation(compose.uiTest)
            implementation(libs.androidx.test.runner)
            implementation(libs.junit)
            // compose.uiTest drags in Espresso 3.5.0, whose input injection calls
            // InputManager.getInstance - gone in Android 17, so every test dies before it runs.
            implementation(libs.androidx.espresso.core)
            // Declares the ComponentActivity the test host launches into.
            implementation(libs.compose.ui.test.manifest)
        }
        commonMain.dependencies {
            api(projects.core.common)
            implementation(projects.core.model)
            api(projects.core.domain)
            implementation(projects.feature.detail)
            implementation(projects.feature.ui)
            implementation(libs.koin.composeVM)
            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.multiplatform.paging.compose)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.coil.compose)
            implementation(libs.androidx.ui.tooling)
            implementation(libs.maps.compose)
            implementation(libs.play.services.maps)
            implementation(libs.androidx.emoji2.emojipicker)
            implementation(libs.androidx.emoji2.bundled)
        }
    }
}


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
    namespace = "com.desarrollodroide.adventurelog.feature.adventures"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}

kotlin {
    sourceSets {
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


plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.buildConfig) apply false
    alias(libs.plugins.modulegraph)
}

moduleGraphConfig {
    readmePath.set("./README.md")
    heading = "### Module Graph"
    showFullPath = true
}
// A test that never ends is neither red nor green: one looped inside runTest and held a module's
// run at full CPU for twenty minutes, in silence (QA campaign, 2026-09-15). Every test task stops.
subprojects {
    tasks.withType<Test>().configureEach {
        timeout.set(java.time.Duration.ofMinutes(15))
    }
}

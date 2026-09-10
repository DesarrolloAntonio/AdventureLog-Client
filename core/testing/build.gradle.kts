plugins {
    alias(libs.plugins.adventurelog.kotlinMultiplatform)
}

/**
 * Test doubles shared by every module's tests.
 *
 * This is production source rather than a test source set on purpose: Kotlin Multiplatform has no
 * test-fixtures publishing, so a commonTest source set cannot be consumed by another module's
 * commonTest. Nothing ships it - only test source sets depend on it.
 */
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.common)
            api(projects.core.model)
            api(projects.core.domain)
            api(projects.core.network)
            implementation(libs.multiplatform.paging.common)
        }
    }
}

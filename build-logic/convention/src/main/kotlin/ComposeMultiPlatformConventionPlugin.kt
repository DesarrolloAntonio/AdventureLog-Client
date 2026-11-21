import com.desarrollodroide.adventurelog.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

/**
 * Compose UI is Android-only: iOS gets native SwiftUI over the shared ViewModels. Every Compose
 * library goes to androidMain, and the Compose compiler only runs on the Android compilation, so
 * nothing in commonMain can reach Compose and no Compose is compiled for iOS.
 */
class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("jetbrainsCompose").get().get().pluginId)
            apply(libs.findPlugin("compose.compiler").get().get().pluginId)
        }

        extensions.configure<ComposeCompilerGradlePluginExtension> {
            targetKotlinPlatforms.set(setOf(KotlinPlatformType.androidJvm))
        }

        val composeDeps = extensions.getByType<ComposeExtension>().dependencies

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                androidMain {
                    dependencies {
                        implementation(composeDeps.runtime)
                        implementation(composeDeps.foundation)
                        implementation(composeDeps.material3)
                        implementation(composeDeps.materialIconsExtended)
                        implementation(composeDeps.material)
                        implementation(composeDeps.ui)
                        implementation(composeDeps.components.resources)
                        implementation(composeDeps.components.uiToolingPreview)
                    }
                }
            }
        }
    }
}

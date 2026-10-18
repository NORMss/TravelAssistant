import com.normno.travelassistant.libs
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            pluginManager.withPlugin("com.android.application") {
                configureCompose(extensions.getByType<ApplicationExtension>())
            }
            pluginManager.withPlugin("com.android.library") {
                configureCompose(extensions.getByType<LibraryExtension>())
            }

            extensions.getByType<ComposeCompilerGradlePluginExtension>().apply {
                // Полезные имена в трейсах системного профайлера.
                includeSourceInformation.set(true)
                // Отчёты по стабильности и перекомпозиции:
                //   ./gradlew assembleRelease -Pcompose.metrics
                if (providers.gradleProperty("compose.metrics").isPresent) {
                    val dir = layout.buildDirectory.dir("compose-metrics")
                    metricsDestination.set(dir)
                    reportsDestination.set(dir)
                }
            }
        }
    }

    private fun Project.configureCompose(extension: CommonExtension) {
        extension.buildFeatures.compose = true

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))

            add("implementation", libs.findLibrary("androidx-compose-ui").get())
            add("implementation", libs.findLibrary("androidx-compose-foundation").get())
            add("implementation", libs.findLibrary("androidx-compose-material3").get())
            add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            add("implementation", libs.findLibrary("kotlinx-collections-immutable").get())
            // MaterialShapes (Cookie, Clover, Pill…) опирается на graphics-shapes
            add("implementation", libs.findLibrary("androidx-graphics-shapes").get())

            add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
            add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
            add("androidTestImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
        }
    }
}

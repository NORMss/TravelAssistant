import com.normno.travelassistant.configureKotlinJvm
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Чистый Kotlin/JVM-модуль: домен и алгоритмы без зависимости от Android. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            configureKotlinJvm()
            dependencies.add("testImplementation", "org.jetbrains.kotlin:kotlin-test")
        }
    }
}

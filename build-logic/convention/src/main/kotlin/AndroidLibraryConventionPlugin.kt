import com.normno.travelassistant.Config
import com.normno.travelassistant.configureAndroid
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                configureAndroid(this)
                // У библиотек в AGP 9 своего targetSdk нет — он остался только у тестов.
                testOptions.targetSdk = Config.TARGET_SDK
                defaultConfig.testInstrumentationRunner =
                    "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }
}

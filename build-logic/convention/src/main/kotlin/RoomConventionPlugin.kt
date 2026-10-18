import androidx.room.gradle.RoomExtension
import com.normno.travelassistant.libs
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")
            pluginManager.apply("androidx.room")

            extensions.configure<RoomExtension> {
                // Схемы под контролем версий: по ним Room проверяет миграции в тестах.
                schemaDirectory("$projectDir/schemas")
            }

            extensions.configure<KspExtension> {
                // Генерировать Kotlin, а не Java — нужны nullability и value-классы.
                arg("room.generateKotlin", "true")
            }

            dependencies {
                add("implementation", libs.findLibrary("androidx-room-runtime").get())
                add("implementation", libs.findLibrary("androidx-room-ktx").get())
                add("ksp", libs.findLibrary("androidx-room-compiler").get())
                add("testImplementation", libs.findLibrary("androidx-room-testing").get())
            }
        }
    }
}

plugins {
    `kotlin-dsl`
}

group = "com.normno.travelassistant.buildlogic"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "travelassistant.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "travelassistant.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "travelassistant.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("compose") {
            id = "travelassistant.compose"
            implementationClass = "ComposeConventionPlugin"
        }
        register("hilt") {
            id = "travelassistant.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("room") {
            id = "travelassistant.room"
            implementationClass = "RoomConventionPlugin"
        }
        register("jvmLibrary") {
            id = "travelassistant.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
    }
}

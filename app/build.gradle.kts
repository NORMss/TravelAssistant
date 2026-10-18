import java.util.Properties

plugins {
    alias(libs.plugins.travelassistant.android.application)
    alias(libs.plugins.travelassistant.compose)
    alias(libs.plugins.travelassistant.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.normno.travelassistant"

    defaultConfig {
        applicationId = "com.normno.travelassistant"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures { buildConfig = true }

    signingConfigs {
        // Релизный ключ не лежит в репозитории. Путь и пароли приходят из
        // keystore.properties (в .gitignore) или из переменных окружения в CI.
        create("release") {
            val props = rootProject.file("keystore.properties")
            if (props.exists()) {
                val secrets = Properties()
                props.inputStream().use(secrets::load)
                storeFile = rootProject.file(secrets.getProperty("storeFile"))
                storePassword = secrets.getProperty("storePassword")
                keyAlias = secrets.getProperty("keyAlias")
                keyPassword = secrets.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("release")
                ?.takeIf { it.storeFile != null }
                ?: signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.datastore)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)

    implementation(projects.feature.today)
    implementation(projects.feature.trips)
    implementation(projects.feature.itinerary)
    implementation(projects.feature.camera)
    implementation(projects.feature.catalog)
    implementation(projects.feature.places)
    implementation(projects.feature.phrasebook)
    implementation(projects.feature.assistant)
    implementation(projects.feature.settings)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Навигация 3: back stack — это список, которым владеет приложение,
    // а не скрытое состояние внутри контроллера.
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation3)

    implementation(libs.timber)

    testImplementation(projects.core.testing)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

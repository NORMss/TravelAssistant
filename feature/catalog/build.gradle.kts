plugins {
    alias(libs.plugins.travelassistant.android.feature)
}

android { namespace = "com.normno.travelassistant.feature.catalog" }

dependencies {
    implementation(projects.core.vision)
    implementation(libs.kotlinx.serialization.json)
}

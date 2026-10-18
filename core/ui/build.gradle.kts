plugins {
    alias(libs.plugins.travelassistant.android.library)
    alias(libs.plugins.travelassistant.compose)
}

android { namespace = "com.normno.travelassistant.core.ui" }

dependencies {
    api(projects.core.model)
    api(projects.core.designsystem)
    implementation(projects.core.common)
    implementation(libs.kotlinx.datetime)
    implementation(libs.coil.compose)
}

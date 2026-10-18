plugins {
    alias(libs.plugins.travelassistant.android.library)
    alias(libs.plugins.travelassistant.compose)
}

android { namespace = "com.normno.travelassistant.core.designsystem" }

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.graphics.shapes)
    api(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.coil.compose)
}

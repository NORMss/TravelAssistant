plugins {
    alias(libs.plugins.travelassistant.android.feature)
}

android { namespace = "com.normno.travelassistant.feature.camera" }

dependencies {
    implementation(projects.core.vision)

    // CameraX для Compose: CameraXViewfinder работает с SurfaceRequest напрямую,
    // без AndroidView и PreviewView.
    implementation(libs.androidx.camera.compose)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.exifinterface)
    implementation(libs.coil.compose)
}

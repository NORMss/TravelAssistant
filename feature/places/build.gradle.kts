plugins {
    alias(libs.plugins.travelassistant.android.feature)
}

android { namespace = "com.normno.travelassistant.feature.places" }

dependencies {
    implementation(libs.osmdroid.android)
}

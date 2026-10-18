plugins {
    alias(libs.plugins.travelassistant.android.feature)
}

android { namespace = "com.normno.travelassistant.feature.settings" }

dependencies {
    implementation(projects.core.ai)
    implementation(projects.core.datastore)
}

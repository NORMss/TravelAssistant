plugins {
    alias(libs.plugins.travelassistant.android.library)
    alias(libs.plugins.travelassistant.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.normno.travelassistant.core.datastore" }

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    api(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
}

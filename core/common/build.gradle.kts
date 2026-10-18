plugins {
    alias(libs.plugins.travelassistant.android.library)
    alias(libs.plugins.travelassistant.hilt)
}

android { namespace = "com.normno.travelassistant.core.common" }

dependencies {
    api(projects.core.model)
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.datetime)
    implementation(libs.timber)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
}

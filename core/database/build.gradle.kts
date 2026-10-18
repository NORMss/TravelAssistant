plugins {
    alias(libs.plugins.travelassistant.android.library)
    alias(libs.plugins.travelassistant.hilt)
    alias(libs.plugins.travelassistant.room)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.normno.travelassistant.core.database" }

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
}

dependencies {
    testImplementation(projects.core.testing)
    testImplementation(libs.truth)
}

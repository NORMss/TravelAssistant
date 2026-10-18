plugins {
    alias(libs.plugins.travelassistant.android.library)
    alias(libs.plugins.travelassistant.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.normno.travelassistant.core.data" }

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.ai)
    implementation(projects.core.vision)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    testImplementation(projects.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.serialization.json)
}

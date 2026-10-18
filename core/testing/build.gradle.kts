plugins {
    alias(libs.plugins.travelassistant.android.library)
    alias(libs.plugins.travelassistant.hilt)
}

android { namespace = "com.normno.travelassistant.core.testing" }

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    api(libs.junit)
    api(libs.truth)
    api(libs.turbine)
    api(libs.kotlinx.coroutines.test)
    api(libs.kotlinx.datetime)
}

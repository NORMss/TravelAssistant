plugins {
    alias(libs.plugins.travelassistant.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)
}

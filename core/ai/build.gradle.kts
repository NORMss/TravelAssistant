plugins {
    alias(libs.plugins.travelassistant.android.library)
    alias(libs.plugins.travelassistant.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.normno.travelassistant.core.ai" }

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)

    // Gemini Nano на устройстве. Доступность проверяется в рантайме: модель есть не на
    // всех телефонах, и это нормальный, а не исключительный случай.
    implementation(libs.mlkit.genai.prompt)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(projects.core.testing)
}

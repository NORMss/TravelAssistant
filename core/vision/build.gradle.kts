plugins {
    alias(libs.plugins.travelassistant.android.library)
    alias(libs.plugins.travelassistant.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android { namespace = "com.normno.travelassistant.core.vision" }

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.androidx.work.runtime.ktx)

    // Вывод модели появится в фазе 5 — см. docs/VISION.md. Зависимость
    // осознанно не подключена: пустой модуль не должен тянуть в APK 3 МБ
    // нативных библиотек MediaPipe раньше, чем ими начнут пользоваться.

    testImplementation(projects.core.testing)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}

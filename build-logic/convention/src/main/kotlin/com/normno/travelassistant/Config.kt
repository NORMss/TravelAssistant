package com.normno.travelassistant

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Единственное место, где записаны уровни SDK и версия Java. Поднимать их нужно
 * здесь — все модули подхватят автоматически.
 */
object Config {
    /**
     * 37 — не прихоть: androidx.hilt 1.4, material3 1.5 и lifecycle 2.10 требуют
     * компиляции против 37 и отказываются линковаться против 36.
     */
    const val COMPILE_SDK = 37
    const val TARGET_SDK = 37

    /**
     * 26 (Android 8.0) — та же нижняя граница, что у BeijingTrip: покрывает почти все
     * активные устройства и даёт точные будильники, каналы уведомлений и адаптивные иконки.
     * Возможности, требующие больше (dynamic color — 31, Gemini Nano — 31+ и конкретные
     * модели телефонов, AppFunctions — 36), включаются по месту проверкой версии.
     */
    const val MIN_SDK = 26

    val JAVA = JavaVersion.VERSION_17
    val JVM_TARGET = JvmTarget.JVM_17
    const val JVM_TOOLCHAIN = 17

    /** Desugaring даёт java.time и java.util.stream на Android 8–12. */
    const val DESUGAR_JDK_LIBS = "com.android.tools:desugar_jdk_libs:2.1.5"
}

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Общая для всех Android-модулей часть `android { }`.
 *
 * Внимание: в AGP 9 `CommonExtension` больше не параметризован — старые конвенции
 * вида `CommonExtension<*, *, *, *, *, *>` из примеров под AGP 8 не компилируются.
 */
internal fun Project.configureAndroid(extension: CommonExtension) {
    with(extension) {
        compileSdk = Config.COMPILE_SDK

        defaultConfig.minSdk = Config.MIN_SDK

        compileOptions.apply {
            sourceCompatibility = Config.JAVA
            targetCompatibility = Config.JAVA
            isCoreLibraryDesugaringEnabled = true
        }

        packaging.resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "/META-INF/DEPENDENCIES",
            "/META-INF/LICENSE*",
        )

        // Robolectric и тесты, которым нужны строки и темы.
        testOptions.unitTests.isIncludeAndroidResources = true
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions.jvmTarget.set(Config.JVM_TARGET)
    }

    dependencies.add("coreLibraryDesugaring", Config.DESUGAR_JDK_LIBS)
    allowModulesWithoutTests()
}

/**
 * Gradle 9 роняет сборку, если у задачи тестов не нашлось ни одного теста. Для
 * модуля, где тестов пока нет, это не ошибка конфигурации: Hilt всё равно генерирует
 * в тестовый sourceSet свои классы, и задача выглядит «с исходниками, но без тестов».
 * Без этого любой новый модуль ломает `./gradlew test` до написания первого теста.
 */
private fun Project.allowModulesWithoutTests() {
    tasks.withType(Test::class.java).configureEach {
        failOnNoDiscoveredTests.set(false)
    }
}

internal fun Project.configureKotlinJvm() {
    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(Config.JVM_TOOLCHAIN)
    }
    allowModulesWithoutTests()
}

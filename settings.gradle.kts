pluginManagement {
    // Конвенции сборки живут отдельной сборкой: они компилируются раньше проекта
    // и доступны всем модулям как обычные плагины по id.
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "TravelAssistant"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

// core — то, что не знает про конкретные экраны
include(":core:model")        // домен: Trip, Day, Stop, Place. Чистый Kotlin, без Android
include(":core:common")       // диспетчеры, Result, время и часовые пояса
include(":core:designsystem") // тема M3 Expressive, движение, float-компоненты
include(":core:ui")           // composable-блоки, знающие домен (карточка поездки, строка этапа)
include(":core:database")     // Room: схема, DAO, миграции
include(":core:datastore")    // настройки и ключи
include(":core:data")         // репозитории: единственный вход к данным для feature-модулей
include(":core:ai")           // LLM: провайдеры, инструменты, формат обмена планом
include(":core:vision")       // офлайн-распознавание достопримечательностей и пакеты регионов
include(":core:testing")      // фикстуры и правила для тестов

// feature — по одному на раздел интерфейса
include(":feature:today")      // «Сейчас»: текущий этап, обратный отсчёт, действия
include(":feature:trips")      // список поездок, создание, импорт
include(":feature:itinerary")  // план: дни, таймлайн, редактор, план Б
include(":feature:places")     // карта и места
include(":feature:phrasebook") // фразы и разговорник
include(":feature:assistant")  // планирование и правка через LLM
include(":feature:camera")     // камера: фотоотчёт и распознавание
include(":feature:catalog")    // готовые маршруты: каталог, скачивание, применение
include(":feature:settings")   // настройки, ключи моделей, экспорт/импорт

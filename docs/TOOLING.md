# Инструменты

## Что установлено и проверено

| Инструмент | Версия | Где |
|---|---|---|
| `android` CLI | 1.0.16625370 | `~/.local/bin/android` |
| Android SDK | platform 36 и **37**, build-tools 35/36/36.1/**37** | `~/Library/Android/sdk` |
| Эмулятор | AVD `Medium_Phone_API_36.1` | запускается, приложение на нём проверено |
| JDK | 21 (Homebrew) + 17 (Oracle) | сборка идёт на 21, байткод — 17 |
| Node, Python, git, gh | 22.22 / 3.14 / 2.55 / 2.91 | |

Платформа 37 и build-tools 37 **доустановлены в ходе работы** — без них
`androidx.hilt` 1.4 и `material3` 1.5 не линкуются.

## Скиллы Android в проекте

Установлены в `.claude/skills/` (17 штук). Загружаются через `Skill` по имени;
в [ROADMAP.md](ROADMAP.md) у каждой задачи указано, какой нужен.

```
adaptive                     адаптивные раскладки, Nav3 Scenes, MediaQuery
navigation-3                 Navigation 3: deep links, несколько стеков, сцены, Hilt
styles                       Jetpack Compose Styles API (material3 1.5)
edge-to-edge                 отступы, IME, перекрытые системными панелями элементы
jetpack-compose-performance  перекомпозиции, дропнутые кадры
android-profiler             системные трейсы, Perfetto, ANR, батарея, память
testing-setup                unit, UI, скриншотные и end-to-end тесты
android-permissions-security аудит разрешений и IPC
android-intent-security      Intent Redirection, обработка входящих интентов
r8-analyzer                  keep-правила, размер APK
play-policy-insights         соответствие политикам Google Play, Data Safety
glance-canonical-layouts     виджеты на Glance
ml-kit-genai-prompt-api      Gemini Nano на устройстве
appfunctions                 приложение как MCP-сервер для системного Gemini
navigation-event             предиктивный «назад»
camerax                      камера для фотодневника
restore-credentials          молчаливый вход после восстановления на новом телефоне
```

**Скиллы не лежат в репозитории** — это чужой код, два мегабайта и 240 файлов.
После клонирования поставить:

```bash
android skills add adaptive navigation-3 styles edge-to-edge \
  jetpack-compose-performance testing-setup android-permissions-security \
  android-intent-security r8-analyzer play-policy-insights \
  glance-canonical-layouts ml-kit-genai-prompt-api appfunctions \
  android-profiler navigation-event camerax restore-credentials litert-runtime \
  --project . --agent claude-code
```

Обновлять: `android skills update`. Список доступных: `android skills list`.

## Команды `android`, которые нужны чаще всего

```bash
android run --apks app/build/outputs/apk/debug/app-debug.apk   # поставить и запустить
android screen capture -o shot.png                             # скриншот
android screen capture --annotate -o shot.png                  # с номерами областей
android layout --flat -o layout.json                           # дерево UI в JSON
android docs search "<запрос>"                                 # официальная документация
android emulator start <avd> / stop <avd>
android sdk install platforms/android-38                       # когда выйдет
```

Если подключено больше одного устройства, нужен `--device <serial>`
(`adb devices` покажет серийники). У автора подключён ещё и телефон по
Wi-Fi — про `--device emulator-5554` легко забыть.

`android layout` и `android screen capture --annotate` — основной способ
проверить экран без человека: дамп даёт текст, идентификаторы и координаты
центров, по которым можно тапать через `adb shell input tap`.

## Чего ещё не хватает

### Репозиторий

`gh` авторизован как `NORMss`, remote указывает на
<https://github.com/NORMss/TravelAssistant> — публичный репозиторий.

Публичность важна помнить при любой работе с данными: всё, что попадает в
`core/data/src/main/assets/templates/`, становится общедоступным. Защита —
`DemoTemplatePrivacyTest`, и она должна быть в CI (задача 9.8).

### Android Studio — опционально, но заметно помогает

`android studio check` сейчас отвечает «No running Studio instances found».
С запущенной Studio (Quail 1+) появляется:

```bash
android studio analyze-file <path>        # разбор файла инспекциями IDE
android studio find-usages <symbol>       # использования по всему проекту
android studio render-compose-preview     # отрисовка @Preview без эмулятора
android studio version-lookup <artifact>  # актуальные версии зависимостей
```

`render-compose-preview` особенно ценен: превью проверяется без сборки и
установки APK — это десятки секунд вместо нескольких минут на каждую правку
вёрстки.

### MCP-серверы

Подключены сейчас: Claude Docs, Figma, Google Drive.

**Figma MCP уже пригоден** — если макеты появятся, из них собирается Compose
напрямую, без ручного переноса отступов.

Про `modelcontextprotocol/kotlin-sdk`, о котором был вопрос: это SDK для
**написания** MCP-клиента или сервера на Kotlin — то есть инструмент
приложения, а не разработки. Он записан в каталог версий (`mcp-kotlin-sdk`)
и относится к фазе 5.4: приложение ходит к удалённым MCP-серверам за данными,
которых у него нет (расписания, бронирования, погода).

Для **разработки** дополнительные MCP пока не нужны: `android` CLI закрывает
устройство, документацию и SDK, а Gradle и файлы доступны напрямую. Если
понадобится — ставится так:

```bash
claude mcp add <имя> -- npx -y <пакет>
```

## Настройки репозитория для агентов

`.claude/settings.json` разрешает без переспрашивания то, что в этом проекте
безопасно и делается постоянно: `./gradlew`, `android`, `adb`, чтение файлов.
Всё, что меняет систему или уходит наружу, по-прежнему спрашивает.

Если прерывания всё равно частые — `/fewer-permission-prompts` посмотрит
историю и предложит дополнить список.

## Проверка после правки UI

Ручной цикл, который стоит повторять при каждом заметном изменении вёрстки:

```bash
./gradlew :app:assembleDebug \
  && android run --apks app/build/outputs/apk/debug/app-debug.apk --device emulator-5554 \
  && sleep 4 \
  && android screen capture --device emulator-5554 -o /tmp/shot.png
```

И посмотреть на PNG. Скриншот ловит то, чего не видит ни один тест:
перекрытый системной панелью элемент, оторванную подпись, съехавший отступ.

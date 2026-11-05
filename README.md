# UI-автотесты Яндекс Маркета

UI-тесты и скриншот-тесты для https://market.yandex.ru.

**Стек:** Java 25, Gradle 9 (Kotlin DSL), Selenide 7, JUnit 5, Allure, AssertJ, image-comparison.

## Быстрый старт

```bash
./gradlew test          # все тесты: Chrome, desktop
./gradlew allureServe   # открыть Allure-отчёт
```

Для запуска самого Gradle нужна Java 17+ (`java -version`). JDK 25 для тестов ставить не нужно: Gradle скачает его сам (toolchain + foojay). Chrome, Firefox или Edge должны быть установлены локально. Драйверы скачивает Selenium Manager.

## Задачи

| Команда | Что запускает |
|---|---|
| `./gradlew test` | все тесты (фильтр: `-Ptags`, `-PexcludeTags`) |
| `./gradlew uiTest` | функциональные UI-тесты (`@UiTest`, тег `ui`) |
| `./gradlew screenshotTest` | скриншот-тесты (`@ScreenshotTest`, тег `screenshot`) |
| `./gradlew allureReport` / `allureServe` | собрать / открыть Allure-отчёт |

Запуск одного теста: `./gradlew test --tests '*SearchTest*'`.

## Параметры прогона

Значения по умолчанию лежат в `gradle.properties`. Любое из них переопределяется через `-P<имя>=<значение>`. Если тест запускается не через Gradle, параметр можно задать через `-Dui.<имя>=...` или переменную окружения `UI_<ИМЯ>` (например, `UI_BROWSER=firefox`).

| Параметр | По умолчанию | Описание |
|---|---|---|
| `browser` | `chrome` | `chrome`, `firefox`, `edge`, `safari` |
| `browserVersion` | — | версия браузера (для Selenoid или Grid) |
| `browserArgs` | — | доп. аргументы браузера через запятую, например `--no-sandbox` |
| `mode` | `desktop` | `desktop` или `mobile` |
| `device` | `iPhone 12 Pro` | устройство для mobile: имя из Chrome DevTools или `ШИРИНАxВЫСОТА@DPR` (`390x844@3`) |
| `userAgent` | — | свой User-Agent для mobile (для custom-устройства и Firefox) |
| `resolution` | `1920x1080` | размер окна в desktop |
| `headless` | `false` | запуск без окна (см. «Ограничения») |
| `remote` | — | URL Selenoid или Selenium Grid |
| `enableVnc`, `enableVideo` | `true`, `false` | VNC и видео в Selenoid |
| `baseUrl` | `https://market.yandex.ru` | адрес стенда |
| `timeout`, `pageLoadTimeout` | `10000`, `30000` | таймауты, мс |
| `threads` | `1` | число параллельных потоков JUnit |
| `tags`, `excludeTags` | — | фильтр по тегам: `ui`, `screenshot`, `smoke` |
| `updateBaseline` | `false` | перезаписать эталонные скриншоты |
| `screenshotTolerance` | `0.5` | допустимый % отличающихся пикселей |
| `ignoreFailures` | `false` | не валить сборку при падении тестов (удобно в CI) |

Примеры:

```bash
./gradlew test -Pbrowser=firefox
./gradlew test -Pmode=mobile -Pdevice="Pixel 7"
./gradlew test -Pmode=mobile -Pdevice=360x800@3
./gradlew test -Ptags=smoke -Pthreads=3
./gradlew uiTest -Pbrowser=edge -Presolution=1366x768
```

### Мобильный режим

- **Chrome и Edge** используют встроенную эмуляцию устройства (`mobileEmulation`): viewport, DPR, touch и мобильный User-Agent. Маркет отдаёт мобильную вёрстку.
- **Firefox** такой эмуляции не умеет. В нём меняется только размер окна, а User-Agent нужно задать через `-PuserAgent=...`.
- **Safari** работает только в desktop.

Тесты, помеченные `@DesktopOnly` или `@MobileOnly`, автоматически пропускаются в неподходящем режиме.

## Скриншот-тесты

```bash
./gradlew screenshotTest                       # сравнить с эталонами
./gradlew screenshotTest -PupdateBaseline=true # перезаписать эталоны
```

- Эталоны лежат в `src/test/screenshots/<browser>/<desktop-разрешение | mobile-устройство>/<имя>.png`. Их нужно коммитить.
- Если эталона нет, тест сохраняет текущий скриншот как эталон и помечается **пропущенным**. Проверьте картинку и закоммитьте её.
- При расхождении в `build/screenshots/...` сохраняются `*-actual.png` и `*-diff.png`. В Allure прикладываются эталон, факт и diff.
- Перед съёмкой отключаются анимации и курсор, а динамические элементы скрываются через `.hide(...)`. Снимаются отдельные элементы, а не страница целиком.
- Скриншоты зависят от ОС, шрифтов и версии браузера. Эталоны, снятые на macOS, не совпадут с Linux в CI. Для стабильности снимайте и сравнивайте их в одном окружении, удобнее всего в Docker.

```java
VisualCheck.of("search-price-filter", results.priceFilter())
        .hide("[data-auto=filter-range-glprice] label") // цены меняются
        .tolerance(1.0)
        .verify();
```

## Selenoid и Selenium Grid

```bash
# Selenoid: образы браузеров скачиваются заранее
docker pull selenoid/vnc_chrome:128.0
docker pull selenoid/vnc_firefox:125.0
docker compose up -d
./gradlew test -Premote=http://localhost:4444/wd/hub    # UI: http://localhost:8080

# Selenium Grid со свежим Chromium (в том числе для Apple Silicon)
docker compose --profile grid up -d
./gradlew test -Premote=http://localhost:4445/wd/hub    # noVNC: http://localhost:7900, пароль secret
```

Образы браузеров для Selenoid больше не обновляются: последние версии — Chrome 128 и Firefox 125. Кроме того, они только amd64. Для актуального браузера используйте профиль `grid`.

## CI

`.github/workflows/ui-tests.yml` запускается на pull request или вручную (Actions → UI tests → Run workflow, с выбором браузера, режима и тегов). Браузер работает в обычном режиме на виртуальном дисплее (xvfb). Allure-отчёт и скриншоты расхождений публикуются как артефакты.

## Структура

```
src/test/java/ru/yandex/market/
  config/       TestConfig, Browser, Mode, MobileDevice — параметры прогона
  driver/       настройка Selenide и capabilities браузеров
  extensions/   жизненный цикл браузера, фильтр desktop/mobile, распознавание капчи
  annotations/  @UiTest, @ScreenshotTest, @DesktopOnly, @MobileOnly
  pages/        page objects (+ components/: шапка, сниппет товара, попапы)
  screenshot/   VisualCheck — сравнение со скриншотом-эталоном
  tests/ui, tests/screenshot
src/test/screenshots/   эталонные скриншоты
```

Локаторы по возможности строятся на атрибутах `data-auto` и `data-zone-name`, а не на сгенерированных CSS-классах.

## Ограничения: защита Маркета от ботов

Маркет показывает капчу или отвечает 403 на автоматизированный трафик. Чаще всего это происходит в **headless**-режиме, с облачных IP (CI, облачные Grid) и при частых запусках. Фреймворк эту защиту **не обходит**. Он распознаёт её (`SiteGuard`) и завершает тест с понятным сообщением `SiteBlockedException`. В Allure такой тест получает статус *broken*, а не *failed*, чтобы его не путали с багом продукта.

Самый надёжный вариант — запускать тесты локально в обычном окне браузера.

plugins {
    java
    alias(libs.plugins.allure)
}

group = "ru.yandex.market"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.selenide)
    testImplementation(libs.allure.selenide)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.assertj)
    testImplementation(libs.image.comparison)
    testRuntimeOnly(libs.slf4j.simple)
}

allure {
    adapter {
        allureJavaVersion = libs.versions.allure
        aspectjVersion = libs.versions.aspectj
        aspectjWeaver = true
        frameworks {
            jupiter {
                enabled = true
            }
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

/** Параметры прогона: имя Gradle-свойства -> system property, которую читает TestConfig. */
val uiProperties = listOf(
    "browser", "browserVersion", "browserArgs", "mode", "device", "userAgent", "resolution", "headless",
    "remote", "enableVnc", "enableVideo", "baseUrl", "timeout", "pageLoadTimeout",
    "updateBaseline", "screenshotTolerance",
)

fun prop(name: String): String? =
    providers.gradleProperty(name).orNull?.takeIf { it.isNotBlank() }

fun csv(name: String): List<String> =
    prop(name)?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

fun Test.configureUiTest(includeTags: List<String> = emptyList()) {
    useJUnitPlatform {
        val tags = includeTags.ifEmpty { csv("tags") }
        if (tags.isNotEmpty()) includeTags(*tags.toTypedArray())
        val excluded = csv("excludeTags")
        if (excluded.isNotEmpty()) excludeTags(*excluded.toTypedArray())
    }

    uiProperties.forEach { name -> prop(name)?.let { systemProperty("ui.$name", it) } }
    systemProperty("ui.baselineDir", layout.projectDirectory.dir("src/test/screenshots").asFile.absolutePath)
    systemProperty("ui.screenshotOutputDir", layout.buildDirectory.dir("screenshots").get().asFile.absolutePath)
    systemProperty("selenide.reportsFolder", layout.buildDirectory.dir("reports/selenide").get().asFile.absolutePath)

    val threads = prop("threads")?.toInt() ?: 1
    systemProperty("junit.jupiter.execution.parallel.enabled", threads > 1)
    systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", threads)

    // Параметры меняются от запуска к запуску, кеш результатов тестов не нужен
    outputs.upToDateWhen { false }
    // Не останавливать сборку при падении тестов, чтобы отчёт всегда собирался
    ignoreFailures = prop("ignoreFailures")?.toBoolean() ?: false

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }
}

tasks.test {
    description = "Все UI-тесты (фильтр через -Ptags / -PexcludeTags)."
    configureUiTest()
}

tasks.register<Test>("uiTest") {
    group = "verification"
    description = "Функциональные UI-тесты (тег ui)."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    configureUiTest(listOf("ui"))
}

tasks.register<Test>("screenshotTest") {
    group = "verification"
    description = "Скриншот-тесты (тег screenshot). Эталоны: -PupdateBaseline=true."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    configureUiTest(listOf("screenshot"))
}

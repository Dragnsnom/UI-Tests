package ru.yandex.market.driver;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.market.config.TestConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Однократная настройка Selenide под текущую конфигурацию. */
public final class DriverConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(DriverConfigurator.class);
    private static volatile boolean configured;

    private DriverConfigurator() {
    }

    public static synchronized void configure() {
        if (configured) {
            return;
        }
        TestConfig cfg = TestConfig.get();
        LOG.info("Конфигурация прогона: {}", cfg);

        Configuration.baseUrl = cfg.baseUrl();
        Configuration.browser = cfg.browser().selenideName();
        cfg.browserVersion().ifPresent(v -> Configuration.browserVersion = v);
        Configuration.headless = cfg.headless();
        Configuration.remote = cfg.remote().orElse(null);
        Configuration.timeout = cfg.timeout();
        Configuration.pageLoadTimeout = cfg.pageLoadTimeout();
        // Маркет долго догружает рекламу и счётчики — ждём только DOM
        Configuration.pageLoadStrategy = "eager";
        Configuration.browserSize = cfg.isMobile()
                ? cfg.device().width() + "x" + (cfg.device().height() + 120)
                : cfg.resolution();
        Configuration.browserCapabilities = CapabilitiesFactory.create(cfg);
        Configuration.screenshots = true;
        Configuration.savePageSource = false;

        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true)
                .savePageSource(true)
                .includeSelenideSteps(true));
        writeAllureEnvironment(cfg);
        configured = true;
    }

    /** Параметры прогона во вкладке Environment отчёта Allure. */
    private static void writeAllureEnvironment(TestConfig cfg) {
        Path dir = Path.of(System.getProperty("allure.results.directory", "build/allure-results"));
        String content = String.join(System.lineSeparator(),
                "browser=" + cfg.browser().selenideName() + cfg.browserVersion().map(v -> " " + v).orElse(""),
                "mode=" + cfg.mode().id(),
                "device=" + (cfg.isMobile() ? cfg.device().name() : cfg.resolution()),
                "headless=" + cfg.headless(),
                "remote=" + cfg.remote().orElse("local"),
                "baseUrl=" + cfg.baseUrl());
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("environment.properties"), content);
        } catch (IOException e) {
            LOG.warn("Не удалось записать environment.properties для Allure", e);
        }
    }
}

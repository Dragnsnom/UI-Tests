package ru.yandex.market.driver;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariOptions;
import ru.yandex.market.config.MobileDevice;
import ru.yandex.market.config.TestConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Собирает capabilities браузера под текущую конфигурацию. */
final class CapabilitiesFactory {

    private CapabilitiesFactory() {
    }

    static MutableCapabilities create(TestConfig cfg) {
        MutableCapabilities caps = switch (cfg.browser()) {
            case CHROME -> chromium(new ChromeOptions(), cfg);
            case EDGE -> chromium(new EdgeOptions(), cfg);
            case FIREFOX -> firefox(cfg);
            case SAFARI -> safari(cfg);
        };
        cfg.remote().ifPresent(url -> caps.setCapability("selenoid:options", selenoidOptions(cfg)));
        return caps;
    }

    private static MutableCapabilities chromium(ChromiumOptions<?> options, TestConfig cfg) {
        options.addArguments("--lang=ru-RU", "--disable-notifications", "--disable-search-engine-choice-screen");
        options.setExperimentalOption("prefs", Map.of("intl.accept_languages", "ru-RU,ru"));
        options.addArguments(cfg.browserArgs());
        if (cfg.isMobile()) {
            options.setExperimentalOption("mobileEmulation", mobileEmulation(cfg));
        } else {
            // Одинаковый масштаб на Retina и обычных мониторах — важно для скриншот-тестов
            options.addArguments("--force-device-scale-factor=1");
        }
        return options;
    }

    /** Параметры Chrome DevTools mobile emulation: имя устройства либо явные метрики. */
    private static Map<String, Object> mobileEmulation(TestConfig cfg) {
        MobileDevice device = cfg.device();
        Map<String, Object> emulation = new HashMap<>();
        if (device.custom()) {
            emulation.put("deviceMetrics", Map.of(
                    "width", device.width(),
                    "height", device.height(),
                    "pixelRatio", device.pixelRatio(),
                    "mobile", true,
                    "touch", true));
            cfg.userAgent().ifPresent(ua -> emulation.put("userAgent", ua));
        } else {
            emulation.put("deviceName", device.name());
        }
        return emulation;
    }

    private static MutableCapabilities firefox(TestConfig cfg) {
        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("intl.accept_languages", "ru-RU,ru");
        options.addPreference("dom.webnotifications.enabled", false);
        options.addArguments(cfg.browserArgs());
        if (cfg.isMobile()) {
            // В Firefox нет встроенной эмуляции устройств: только размер окна и, если задан, User-Agent
            cfg.userAgent().ifPresent(ua -> options.addPreference("general.useragent.override", ua));
        }
        return options;
    }

    private static MutableCapabilities safari(TestConfig cfg) {
        if (cfg.isMobile()) {
            throw new IllegalStateException("Safari не поддерживает мобильный режим. Используйте -Pbrowser=chrome -Pmode=mobile");
        }
        return new SafariOptions();
    }

    private static Map<String, Object> selenoidOptions(TestConfig cfg) {
        return Map.of(
                "enableVNC", cfg.enableVnc(),
                "enableVideo", cfg.enableVideo(),
                "screenResolution", cfg.resolution() + "x24",
                "timeZone", "Europe/Moscow",
                "env", List.of("LANG=ru_RU.UTF-8", "LANGUAGE=ru:en", "LC_ALL=ru_RU.UTF-8"));
    }
}

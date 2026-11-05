package ru.yandex.market.config;

import java.util.Arrays;
import java.util.Locale;

/** Поддерживаемые браузеры. */
public enum Browser {
    CHROME("chrome", true),
    EDGE("edge", true),
    FIREFOX("firefox", false),
    SAFARI("safari", false);

    private final String selenideName;
    private final boolean chromium;

    Browser(String selenideName, boolean chromium) {
        this.selenideName = selenideName;
        this.chromium = chromium;
    }

    public String selenideName() {
        return selenideName;
    }

    /** Chromium-браузеры умеют полноценную эмуляцию мобильного устройства (mobileEmulation). */
    public boolean isChromium() {
        return chromium;
    }

    public static Browser from(String value) {
        return Arrays.stream(values())
                .filter(b -> b.selenideName.equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Неизвестный браузер '%s'. Допустимо: %s".formatted(value, Arrays.toString(values()).toLowerCase(Locale.ROOT))));
    }
}

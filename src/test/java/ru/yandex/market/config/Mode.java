package ru.yandex.market.config;

import java.util.Arrays;
import java.util.Locale;

/** Режим прогона: десктопная или мобильная версия сайта. */
public enum Mode {
    DESKTOP, MOBILE;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Mode from(String value) {
        return Arrays.stream(values())
                .filter(m -> m.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Неизвестный режим '%s'. Допустимо: desktop, mobile".formatted(value)));
    }
}

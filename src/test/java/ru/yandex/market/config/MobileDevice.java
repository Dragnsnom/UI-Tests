package ru.yandex.market.config;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Описание эмулируемого мобильного устройства.
 * <p>
 * Задаётся параметром {@code device}: либо имя устройства из Chrome DevTools ("iPhone 12 Pro", "Pixel 7"),
 * либо явные метрики в формате {@code WIDTHxHEIGHT@DPR}, например {@code 390x844@3}.
 */
public record MobileDevice(String name, int width, int height, double pixelRatio, boolean custom) {

    private static final Pattern METRICS = Pattern.compile("(\\d+)x(\\d+)(?:@([\\d.]+))?");

    /** Размеры известных устройств — нужны для Firefox и для имени папки с эталонами. */
    private static final Map<String, MobileDevice> PRESETS = Map.of(
            "iphone 12 pro", new MobileDevice("iPhone 12 Pro", 390, 844, 3, false),
            "iphone 14 pro max", new MobileDevice("iPhone 14 Pro Max", 430, 932, 3, false),
            "iphone se", new MobileDevice("iPhone SE", 375, 667, 2, false),
            "pixel 7", new MobileDevice("Pixel 7", 412, 915, 2.625, false),
            "samsung galaxy s20 ultra", new MobileDevice("Samsung Galaxy S20 Ultra", 412, 915, 3.5, false)
    );

    public static MobileDevice parse(String value) {
        String v = value.trim();
        Matcher m = METRICS.matcher(v);
        if (m.matches()) {
            double dpr = m.group(3) == null ? 2 : Double.parseDouble(m.group(3));
            return new MobileDevice(v, Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), dpr, true);
        }
        MobileDevice preset = PRESETS.get(v.toLowerCase(Locale.ROOT));
        // Неизвестное имя передаём в Chrome как есть; размер окна берём типовой
        return preset != null ? preset : new MobileDevice(v, 390, 844, 3, false);
    }

    /** Имя для путей к файлам: "iPhone 12 Pro" -> "iphone-12-pro". */
    public String slug() {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}

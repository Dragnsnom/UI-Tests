package ru.yandex.market.config;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Конфигурация прогона.
 * <p>
 * Источники по приоритету: system property {@code ui.<name>} (Gradle пробрасывает туда {@code -P<name>}),
 * затем переменная окружения {@code UI_<NAME>} (например {@code UI_BROWSER}), затем значение по умолчанию.
 */
public final class TestConfig {

    private static final TestConfig INSTANCE = new TestConfig();

    private final Browser browser = Browser.from(prop("browser", "chrome"));
    private final String browserVersion = prop("browserVersion", "");
    private final Mode mode = Mode.from(prop("mode", "desktop"));
    private final MobileDevice device = MobileDevice.parse(prop("device", "iPhone 12 Pro"));
    private final String userAgent = prop("userAgent", "");
    private final String resolution = prop("resolution", "1920x1080");
    private final List<String> browserArgs = Arrays.stream(prop("browserArgs", "").split(","))
            .map(String::trim).filter(a -> !a.isEmpty()).toList();
    private final boolean headless = Boolean.parseBoolean(prop("headless", "false"));
    private final String remote = prop("remote", "");
    private final boolean enableVnc = Boolean.parseBoolean(prop("enableVnc", "true"));
    private final boolean enableVideo = Boolean.parseBoolean(prop("enableVideo", "false"));
    private final String baseUrl = prop("baseUrl", "https://market.yandex.ru");
    private final long timeout = Long.parseLong(prop("timeout", "10000"));
    private final long pageLoadTimeout = Long.parseLong(prop("pageLoadTimeout", "30000"));
    private final boolean updateBaseline = Boolean.parseBoolean(prop("updateBaseline", "false"));
    private final double screenshotTolerance = Double.parseDouble(prop("screenshotTolerance", "0.5"));
    private final Path baselineDir = Path.of(prop("baselineDir", "src/test/screenshots"));
    private final Path screenshotOutputDir = Path.of(prop("screenshotOutputDir", "build/screenshots"));

    private TestConfig() {
    }

    public static TestConfig get() {
        return INSTANCE;
    }

    private static String prop(String name, String defaultValue) {
        String value = System.getProperty("ui." + name);
        if (isBlank(value)) {
            value = System.getenv("UI_" + name.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT));
        }
        return isBlank(value) ? defaultValue : value.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public Browser browser() { return browser; }
    public Optional<String> browserVersion() { return Optional.of(browserVersion).filter(s -> !s.isBlank()); }
    public Mode mode() { return mode; }
    public boolean isMobile() { return mode == Mode.MOBILE; }
    public MobileDevice device() { return device; }
    public Optional<String> userAgent() { return Optional.of(userAgent).filter(s -> !s.isBlank()); }
    public String resolution() { return resolution; }
    /** Дополнительные аргументы командной строки браузера, например --no-sandbox. */
    public List<String> browserArgs() { return browserArgs; }
    public boolean headless() { return headless; }
    public Optional<String> remote() { return Optional.of(remote).filter(s -> !s.isBlank()); }
    public boolean enableVnc() { return enableVnc; }
    public boolean enableVideo() { return enableVideo; }
    public String baseUrl() { return baseUrl; }
    public long timeout() { return timeout; }
    public long pageLoadTimeout() { return pageLoadTimeout; }
    public boolean updateBaseline() { return updateBaseline; }
    public double screenshotTolerance() { return screenshotTolerance; }
    public Path baselineDir() { return baselineDir; }
    public Path screenshotOutputDir() { return screenshotOutputDir; }

    /** Профиль окружения для раскладки эталонов: chrome/desktop, chrome/mobile-iphone-12-pro ... */
    public String profile() {
        String modePart = isMobile() ? "mobile-" + device.slug() : "desktop-" + resolution;
        return browser.selenideName() + "/" + modePart;
    }

    @Override
    public String toString() {
        return "browser=%s%s, mode=%s%s, headless=%s, remote=%s, baseUrl=%s".formatted(
                browser.selenideName(), browserVersion().map(v -> ":" + v).orElse(""),
                mode.id(), isMobile() ? " (" + device.name() + ")" : " (" + resolution + ")",
                headless, remote().orElse("-"), baseUrl);
    }
}

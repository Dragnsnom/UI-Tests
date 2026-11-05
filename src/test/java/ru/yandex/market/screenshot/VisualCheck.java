package ru.yandex.market.screenshot;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.junit.jupiter.api.Assumptions;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.market.config.TestConfig;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import static com.codeborne.selenide.Condition.visible;

/**
 * Скриншот-проверка элемента против эталона.
 * <pre>{@code
 * VisualCheck.of("search-row", header.searchRow())
 *         .hide("[data-auto=headerFortuneWheelMenuItem]")
 *         .verify();
 * }</pre>
 * Эталоны лежат в {@code src/test/screenshots/<browser>/<mode>/<name>.png}.
 * Если эталона нет, он создаётся, а тест помечается пропущенным. {@code -PupdateBaseline=true} перезаписывает эталоны.
 */
public final class VisualCheck {

    private static final Logger LOG = LoggerFactory.getLogger(VisualCheck.class);
    private static final String STYLE_ID = "ui-tests-visual-stabilizer";

    private final String name;
    private final SelenideElement element;
    private final List<String> hidden = new ArrayList<>();
    private double tolerance = TestConfig.get().screenshotTolerance();

    private VisualCheck(String name, SelenideElement element) {
        this.name = name;
        this.element = element;
    }

    public static VisualCheck of(String name, SelenideElement element) {
        return new VisualCheck(name, element);
    }

    /** Скрыть динамические элементы (visibility: hidden — место под ними сохраняется). */
    public VisualCheck hide(String... cssSelectors) {
        hidden.addAll(List.of(cssSelectors));
        return this;
    }

    /** Допустимый процент отличающихся пикселей для этой проверки. */
    public VisualCheck tolerance(double percent) {
        this.tolerance = percent;
        return this;
    }

    @Step("Сравнить скриншот «{this.name}» с эталоном")
    public void verify() {
        TestConfig cfg = TestConfig.get();
        Path baseline = cfg.baselineDir().resolve(cfg.profile()).resolve(name + ".png");
        Path outDir = cfg.screenshotOutputDir().resolve(cfg.profile());

        BufferedImage actual = capture();

        if (cfg.updateBaseline() || Files.notExists(baseline)) {
            boolean created = Files.notExists(baseline);
            write(actual, baseline);
            attach("Новый эталон", actual);
            if (created && !cfg.updateBaseline()) {
                Assumptions.abort("Эталона не было, он создан: " + baseline + ". Проверьте картинку и закоммитьте её.");
            }
            LOG.info("Эталон обновлён: {}", baseline);
            return;
        }

        BufferedImage expected = read(baseline);
        ImageComparisonResult result = new ImageComparison(expected, actual)
                .setAllowingPercentOfDifferentPixels(tolerance)
                .setPixelToleranceLevel(0.1)
                .compareImages();

        if (result.getImageComparisonState() == ImageComparisonState.MATCH) {
            return;
        }

        Path actualFile = outDir.resolve(name + "-actual.png");
        Path diffFile = outDir.resolve(name + "-diff.png");
        write(actual, actualFile);
        BufferedImage diff = result.getResult() != null ? result.getResult() : actual;
        write(diff, diffFile);
        attachDiff(expected, actual, diff);

        String reason = result.getImageComparisonState() == ImageComparisonState.SIZE_MISMATCH
                ? "размер отличается: эталон %dx%d, факт %dx%d".formatted(
                        expected.getWidth(), expected.getHeight(), actual.getWidth(), actual.getHeight())
                : "отличается %.2f%% пикселей (допустимо %.2f%%)".formatted(result.getDifferencePercent(), tolerance);
        throw new AssertionError("Скриншот «%s» не совпал с эталоном: %s.%nЭталон: %s%nФакт: %s%nDiff: %s%n"
                .formatted(name, reason, baseline, actualFile, diffFile)
                + "Если изменение ожидаемое — перезапустите с -PupdateBaseline=true.");
    }

    private BufferedImage capture() {
        element.shouldBe(visible).scrollIntoView("{block: 'center'}");
        injectStabilizer();
        try {
            Selenide.executeAsyncJavaScript(
                    "const done = arguments[arguments.length - 1]; document.fonts.ready.then(() => done(true));");
            // Даём браузеру перерисовать страницу после подмены стилей
            Selenide.sleep(300);
            byte[] png = element.getScreenshotAs(OutputType.BYTES);
            return ImageIO.read(new ByteArrayInputStream(png));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            Selenide.executeJavaScript("document.getElementById(arguments[0])?.remove();", STYLE_ID);
        }
    }

    /** Отключает анимации, мигающий курсор и скрывает динамические элементы. */
    private void injectStabilizer() {
        StringBuilder css = new StringBuilder("""
                *, *::before, *::after {
                  animation: none !important; transition: none !important; caret-color: transparent !important;
                }
                ::-webkit-scrollbar { display: none !important; }
                """);
        if (!hidden.isEmpty()) {
            css.append(String.join(", ", hidden)).append(" { visibility: hidden !important; }");
        }
        Selenide.executeJavaScript("""
                const style = document.createElement('style');
                style.id = arguments[0];
                style.textContent = arguments[1];
                document.head.appendChild(style);
                if (document.activeElement && document.activeElement !== document.body) document.activeElement.blur();
                """, STYLE_ID, css.toString());
    }

    private static BufferedImage read(Path file) {
        try {
            return ImageIO.read(file.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать " + file, e);
        }
    }

    private static void write(BufferedImage image, Path file) {
        try {
            Files.createDirectories(file.getParent());
            ImageIO.write(image, "png", file.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать " + file, e);
        }
    }

    private static byte[] png(BufferedImage image) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void attach(String title, BufferedImage image) {
        Allure.addAttachment(title, "image/png", new ByteArrayInputStream(png(image)), "png");
    }

    /** Вложение в формате screen-diff: Allure показывает эталон, факт и diff с переключателем. */
    private void attachDiff(BufferedImage expected, BufferedImage actual, BufferedImage diff) {
        String json = String.format(Locale.ROOT, "{\"expected\":\"%s\",\"actual\":\"%s\",\"diff\":\"%s\"}",
                dataUri(expected), dataUri(actual), dataUri(diff));
        Allure.addAttachment("Screenshot diff: " + name, "application/vnd.allure.image.diff", json, ".imagediff");
        attach("Эталон", expected);
        attach("Факт", actual);
        attach("Diff", diff);
    }

    private static String dataUri(BufferedImage image) {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png(image));
    }
}

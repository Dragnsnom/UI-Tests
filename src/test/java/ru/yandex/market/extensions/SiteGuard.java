package ru.yandex.market.extensions;

import com.codeborne.selenide.WebDriverRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.market.config.TestConfig;
import ru.yandex.market.utils.Waits;

/** Распознаёт страницы защиты Яндекса (SmartCaptcha, 403), чтобы тест падал с понятной причиной. */
public final class SiteGuard {

    private static final Logger LOG = LoggerFactory.getLogger(SiteGuard.class);

    private SiteGuard() {
    }

    /**
     * С -PcaptchaWait=N и видимым браузером даёт решить капчу руками. После решения Яндекс сам
     * возвращает на исходную страницу, а с -PuserDataDir cookie сохранится для следующих запусков.
     */
    private static void waitForManualSolve() {
        TestConfig cfg = TestConfig.get();
        if (cfg.captchaWait() <= 0 || cfg.headless() || cfg.remote().isPresent()) {
            return;
        }
        LOG.warn("Показана капча — решите её в окне браузера, жду до {} с", cfg.captchaWait());
        if (Waits.until(() -> !isBlocked(), cfg.captchaWait() * 1000)) {
            LOG.info("Капча пройдена, продолжаю");
            Waits.until(() -> "complete".equals(com.codeborne.selenide.Selenide.executeJavaScript("return document.readyState")), 10_000);
        }
    }

    public static boolean isBlocked() {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            return false;
        }
        try {
            String url = WebDriverRunner.url();
            String title = WebDriverRunner.getWebDriver().getTitle();
            return url.contains("/showcaptcha") || "403".equals(title);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Бросает {@link SiteBlockedException}, если открыта страница защиты. */
    public static void checkNotBlocked() {
        checkNotBlocked(null);
    }

    static void checkNotBlocked(Throwable cause) {
        if (isBlocked() && cause == null) {
            waitForManualSolve();
        }
        if (isBlocked()) {
            throw new SiteBlockedException(
                    "Яндекс Маркет не пустил автотест: показана капча или 403 (" + WebDriverRunner.url() + "). "
                    + "Так бывает в headless-режиме, с облачных IP и при частых запусках. "
                    + "Запустите тесты в обычном (не headless) браузере или повторите позже.", cause);
        }
    }
}

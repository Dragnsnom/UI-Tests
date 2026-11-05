package ru.yandex.market.extensions;

import com.codeborne.selenide.WebDriverRunner;

/** Распознаёт страницы защиты Яндекса (SmartCaptcha, 403), чтобы тест падал с понятной причиной. */
public final class SiteGuard {

    private SiteGuard() {
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
        if (isBlocked()) {
            throw new SiteBlockedException(
                    "Яндекс Маркет не пустил автотест: показана капча или 403 (" + WebDriverRunner.url() + "). "
                    + "Так бывает в headless-режиме, с облачных IP и при частых запусках. "
                    + "Запустите тесты в обычном (не headless) браузере или повторите позже.", cause);
        }
    }
}

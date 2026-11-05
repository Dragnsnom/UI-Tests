package ru.yandex.market.extensions;

/**
 * Сайт не пустил тест: показал капчу или ответил 403.
 * <p>
 * Это не баг продукта, а защита от автоматизированного трафика. Исключение не является
 * {@link AssertionError}, поэтому Allure помечает такой тест как broken, а не failed.
 */
public class SiteBlockedException extends RuntimeException {
    public SiteBlockedException(String message, Throwable cause) {
        super(message, cause);
    }
}

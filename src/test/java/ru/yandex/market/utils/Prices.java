package ru.yandex.market.utils;

/** Разбор цен вида "50 014 ₽" (с неразрывными пробелами). */
public final class Prices {

    private Prices() {
    }

    public static int parse(String text) {
        String digits = text.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("В строке нет цены: '" + text + "'");
        }
        return Integer.parseInt(digits);
    }
}

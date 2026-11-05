package ru.yandex.market.utils;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;

import java.util.function.BooleanSupplier;

/** Мягкое ожидание: не бросает исключение, а возвращает результат — проверку делает сам тест. */
public final class Waits {

    private Waits() {
    }

    public static boolean until(BooleanSupplier condition) {
        return until(condition, Configuration.timeout);
    }

    public static boolean until(BooleanSupplier condition, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        do {
            try {
                if (condition.getAsBoolean()) {
                    return true;
                }
            } catch (RuntimeException ignored) {
                // элемент перерисовался — пробуем ещё раз
            }
            Selenide.sleep(250);
        } while (System.currentTimeMillis() < deadline);
        return false;
    }
}

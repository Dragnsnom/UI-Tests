package ru.yandex.market.utils;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.WebElement;
import ru.yandex.market.config.DelayRange;
import ru.yandex.market.config.TestConfig;

/** Действия в «человеческом» темпе, чтобы антибот не принимал прогон за скрипт. */
public final class Human {

    private Human() {
    }

    public static void pause(DelayRange range) {
        if (TestConfig.get().humanize()) {
            long ms = range.random();
            if (ms > 0) {
                Selenide.sleep(ms);
            }
        }
    }

    /** Пауза «подумать» перед действием. */
    public static void pause() {
        pause(TestConfig.get().actionDelay());
    }

    /**
     * Ввод текста по одному символу со случайными паузами.
     * Символы отправляются напрямую в WebElement, чтобы не логировать каждую букву шагом Allure
     * и не получать к каждой букве паузу из {@link ru.yandex.market.extensions.HumanPacingListener}.
     */
    public static SelenideElement type(SelenideElement element, String text) {
        if (!TestConfig.get().humanize()) {
            return element.setValue(text);
        }
        element.clear();
        WebElement raw = element.toWebElement();
        DelayRange delay = TestConfig.get().typingDelay();
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            raw.sendKeys(new String(Character.toChars(cp)));
            i += Character.charCount(cp);
            pause(delay);
        }
        return element;
    }
}

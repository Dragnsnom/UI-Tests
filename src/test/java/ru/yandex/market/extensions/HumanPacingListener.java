package ru.yandex.market.extensions;

import com.codeborne.selenide.logevents.LogEvent;
import com.codeborne.selenide.logevents.LogEventListener;
import ru.yandex.market.utils.Human;

import java.util.Locale;
import java.util.Set;

/**
 * Добавляет случайную паузу перед каждым действием Selenide (клик, ввод, переход, скролл).
 * Проверки (shouldBe/shouldHave) и чтение текста не замедляются.
 */
public class HumanPacingListener implements LogEventListener {

    private static final Set<String> ACTIONS = Set.of(
            "open", "click", "doubleclick", "contextclick", "hover",
            "setvalue", "val", "append", "sendkeys", "type", "clear",
            "pressenter", "presstab", "pressescape", "press",
            "selectoption", "selectoptionbyvalue", "selectradio",
            "scrollto", "scrollintoview", "back", "forward", "refresh");

    @Override
    public void beforeEvent(LogEvent event) {
        if (isAction(event.getElement()) || isAction(event.getSubject())) {
            Human.pause();
        }
    }

    @Override
    public void afterEvent(LogEvent event) {
    }

    /** "set value(\"iphone\")" -> "setvalue". */
    private static boolean isAction(String text) {
        if (text == null) {
            return false;
        }
        int paren = text.indexOf('(');
        String name = (paren >= 0 ? text.substring(0, paren) : text)
                .replace(" ", "").toLowerCase(Locale.ROOT);
        return ACTIONS.contains(name);
    }
}

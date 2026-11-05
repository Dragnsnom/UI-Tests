package ru.yandex.market.pages.components;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;

/** Закрывает всплывающие окна, которые мешают тестам: уточнение адреса, промо приложения, авторизация. */
public final class Popups {

    /** Контейнеры попапов. */
    private static final String POPUPS = String.join(", ",
            "[data-zone-name=addressesPopup]",
            "[data-zone-name=authAgitationPopup]",
            "[data-zone-name=app-distribution-new]",
            "[data-zone-name=madvPopup]");

    /** Кнопки закрытия внутри попапов. */
    private static final String CLOSE_BUTTONS = String.join(", ",
            "[data-auto=close-popup]",
            "[data-zone-name=cancelButton]",
            "button[aria-label*=Закрыть]",
            "[data-auto=icon-cross]");

    private Popups() {
    }

    /** Без ожиданий: закрывает только то, что видно прямо сейчас. */
    @Step("Закрыть всплывающие окна, если есть")
    public static void dismiss() {
        for (SelenideElement popup : $$(POPUPS).filter(visible)) {
            popup.$$(CLOSE_BUTTONS).filter(visible).asFixedIterable().stream()
                    .findFirst()
                    .ifPresent(SelenideElement::click);
        }
        for (SelenideElement close : $$("[data-auto=close-popup]").filter(visible)) {
            close.click();
        }
    }
}

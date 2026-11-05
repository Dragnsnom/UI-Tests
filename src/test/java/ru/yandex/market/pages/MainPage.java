package ru.yandex.market.pages;

import com.codeborne.selenide.ElementsCollection;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/** Главная страница Маркета. */
public class MainPage extends BasePage<MainPage> {

    @Step("Открыть главную страницу")
    public MainPage open() {
        return openPath("/");
    }

    @Override
    protected void waitForPageElements() {
        $("input[data-auto=search-input]").shouldBe(visible);
    }

    /** Товары в рекомендательной ленте. */
    public ElementsCollection feedSnippets() {
        return $$("[data-zone-name=productSnippet]");
    }

    public com.codeborne.selenide.SelenideElement footer() {
        return $("[data-zone-name=footer]");
    }
}

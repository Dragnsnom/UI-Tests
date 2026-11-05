package ru.yandex.market.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

/** Корзина /my/cart */
public class CartPage extends BasePage<CartPage> {

    @Step("Открыть корзину")
    public CartPage open() {
        return openPath("/my/cart");
    }

    @Override
    protected void waitForPageElements() {
        webdriver().shouldHave(urlContaining("/my/cart"));
        content().shouldBe(visible);
    }

    public SelenideElement content() {
        return $("main, [data-zone-name=main], #root, body");
    }
}

package ru.yandex.market.pages.components;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ru.yandex.market.config.TestConfig;
import ru.yandex.market.pages.CartPage;
import ru.yandex.market.pages.MainPage;
import ru.yandex.market.pages.SearchResultsPage;

import static com.codeborne.selenide.Condition.interactable;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Шапка сайта. Разметка desktop и mobile различается: на mobile нет кнопки «Найти» и логотипа,
 * а корзина живёт в нижнем таббаре.
 */
public class Header {

    private final boolean mobile = TestConfig.get().isMobile();

    public SelenideElement searchRow() {
        return $("[data-auto=search-row]");
    }

    public SelenideElement searchInput() {
        // На mobile по тапу может открыться полноэкранный поиск со своим полем — берём видимое
        return $$("input[data-auto=search-input]").filter(visible).first();
    }

    public SelenideElement logo() {
        return $("a[data-auto=logoMarketLink]");
    }

    public SelenideElement cart() {
        return mobile ? $("[data-auto=cartTab]") : $("[data-auto=header-cart]");
    }

    public SelenideElement tabBar() {
        return $("nav[data-auto=navbar]");
    }

    @Step("Найти «{query}» через строку поиска")
    public SearchResultsPage search(String query) {
        searchInput().shouldBe(interactable).click();
        SelenideElement input = searchInput();
        input.setValue(query);
        if (mobile) {
            input.pressEnter();
        } else {
            $("button[data-auto=search-button]").click();
        }
        return new SearchResultsPage().waitLoaded();
    }

    @Step("Перейти на главную по логотипу")
    public MainPage clickLogo() {
        logo().click();
        return new MainPage().waitLoaded();
    }

    @Step("Открыть корзину")
    public CartPage openCart() {
        cart().$("a").click();
        return new CartPage().waitLoaded();
    }
}

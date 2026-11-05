package ru.yandex.market.tests.ui;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.market.annotations.UiTest;
import ru.yandex.market.pages.CartPage;
import ru.yandex.market.pages.ProductPage;
import ru.yandex.market.pages.SearchResultsPage;
import ru.yandex.market.pages.components.ProductSnippet;
import ru.yandex.market.tests.BaseTest;

import static com.codeborne.selenide.Condition.text;
import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@Feature("Карточка товара")
@DisplayName("Карточка товара и корзина")
class ProductTest extends BaseTest {

    @Test
    @DisplayName("Из выдачи открывается карточка того же товара")
    void openProductFromSearch() {
        ProductSnippet snippet = new SearchResultsPage().open("iphone").snippet(0);
        String expectedTitle = snippet.title();

        ProductPage product = snippet.open();

        assertThat(product.titleText()).isEqualToIgnoringWhitespace(expectedTitle);
        assertThat(product.price()).isPositive();
    }

    @Test
    @DisplayName("Товар добавляется в корзину без авторизации")
    void addProductToCart() {
        ProductPage product = new SearchResultsPage().open("iphone").snippet(0).open();
        String title = product.titleText();

        product.addToCart();
        CartPage cart = new CartPage().open();

        // В корзине название может быть обрезано — сверяем начало
        cart.content().shouldHave(text(title.substring(0, Math.min(title.length(), 25))));
    }
}

package ru.yandex.market.pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ru.yandex.market.utils.Prices;
import ru.yandex.market.utils.Waits;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;

/** Карточка товара /card/... */
public class ProductPage extends BasePage<ProductPage> {

    @Override
    protected void waitForPageElements() {
        title().shouldBe(visible);
    }

    public SelenideElement title() {
        return $$("h1[data-auto=productCardTitle], [data-auto=product-page] h1").filter(visible).first();
    }

    public String titleText() {
        return title().getText().trim();
    }

    /** Основная цена — первая цена в блоке товара (ниже идут похожие товары). */
    public int price() {
        return Prices.parse($$("[data-zone-name=price] [data-auto=snippet-price-current]")
                .filter(visible).first().getText());
    }

    public SelenideElement cartButton() {
        return $$("[data-auto=default-offer-actions] [data-auto=cartButton], [data-auto=cartButton]")
                .filter(visible).first();
    }

    @Step("Добавить товар в корзину")
    public ProductPage addToCart() {
        SelenideElement button = cartButton();
        button.scrollIntoView("{block: 'center'}").click();
        // После добавления кнопка превращается в счётчик; если разметка другая — проверку делает корзина
        Waits.until(() -> !button.exists() || !button.has(text("В корзину")), 5000);
        return this;
    }
}

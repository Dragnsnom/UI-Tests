package ru.yandex.market.pages.components;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Step;
import ru.yandex.market.pages.ProductPage;
import ru.yandex.market.utils.Prices;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.switchTo;

/** Карточка товара в выдаче или ленте. */
public class ProductSnippet {

    private final SelenideElement root;

    public ProductSnippet(SelenideElement root) {
        this.root = root;
    }

    public SelenideElement root() {
        return root;
    }

    public SelenideElement titleElement() {
        return root.$("[data-auto=snippet-title]");
    }

    public String title() {
        return titleElement().shouldBe(visible).getText().trim();
    }

    public int price() {
        return Prices.parse(root.$("[data-auto=snippet-price-current]").shouldBe(visible).getText());
    }

    /** Открывает карточку товара. На desktop она открывается в новой вкладке — переключаемся на неё. */
    @Step("Открыть товар из выдачи")
    public ProductPage open() {
        int windowsBefore = WebDriverRunner.getWebDriver().getWindowHandles().size();
        root.$("a[data-auto=snippet-link]").scrollIntoView("{block: 'center'}").click();
        if (WebDriverRunner.getWebDriver().getWindowHandles().size() > windowsBefore
                || waitNewWindow(windowsBefore)) {
            switchTo().window(windowsBefore);
        }
        return new ProductPage().waitLoaded();
    }

    private static boolean waitNewWindow(int windowsBefore) {
        return ru.yandex.market.utils.Waits.until(
                () -> WebDriverRunner.getWebDriver().getWindowHandles().size() > windowsBefore, 3000);
    }
}

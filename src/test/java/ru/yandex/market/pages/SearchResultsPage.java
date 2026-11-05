package ru.yandex.market.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Step;
import ru.yandex.market.config.TestConfig;
import ru.yandex.market.pages.components.ProductSnippet;
import ru.yandex.market.utils.Waits;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

/** Страница поисковой выдачи /search?text=... */
public class SearchResultsPage extends BasePage<SearchResultsPage> {

    /** Органическая выдача без рекламных врезок (на desktop они в отдельных блоках). */
    private static final String ORGANIC_DESKTOP = "[data-auto=searchOrganic] [data-zone-name=productSnippet]";
    private static final String ANY_SNIPPET = "[data-zone-name=productSnippet]";

    @Step("Открыть выдачу по запросу «{query}»")
    public SearchResultsPage open(String query) {
        return openPath("/search?text=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
    }

    @Override
    protected void waitForPageElements() {
        webdriver().shouldHave(urlContaining("/search"));
        snippets().shouldHave(sizeGreaterThan(0));
    }

    public SelenideElement title() {
        return $$("h1[data-auto=title], [data-zone-name=searchTitle] h1").filter(visible).first();
    }

    public ElementsCollection snippets() {
        boolean desktop = !TestConfig.get().isMobile();
        return $$(desktop ? ORGANIC_DESKTOP : ANY_SNIPPET).filter(visible);
    }

    public ProductSnippet snippet(int index) {
        return new ProductSnippet(snippets().get(index));
    }

    public List<String> titles(int limit) {
        return snippets().asFixedIterable().stream().limit(limit)
                .map(e -> new ProductSnippet(e).title())
                .toList();
    }

    public List<Integer> prices(int limit) {
        return snippets().asFixedIterable().stream().limit(limit)
                .map(e -> new ProductSnippet(e).price())
                .toList();
    }

    public SelenideElement priceFilter() {
        return $("[data-auto=filter-range-glprice]");
    }

    public SelenideElement sortControl() {
        return $("[data-auto=SearchSort]");
    }

    @Step("Задать цену «от» {min} ₽")
    public SearchResultsPage setPriceFrom(int min) {
        SelenideElement input = priceFilter().$("[data-auto=filter-range-min] input").shouldBe(visible);
        input.click();
        input.setValue(String.valueOf(min)).pressTab();
        webdriver().shouldHave(urlContaining("pricefrom=" + min));
        waitResultsUpdated(() -> prices(10).stream().allMatch(p -> p >= min));
        return this;
    }

    @Step("Отсортировать: {order}")
    public SearchResultsPage sortBy(SortOrder order) {
        sortControl().$("[aria-haspopup]").click();
        $$("[data-zone-name=sortItem]").findBy(text(order.label())).shouldBe(visible).click();
        if (order.urlValue() != null) {
            webdriver().shouldHave(urlContaining("how=" + order.urlValue()));
        }
        sortControl().shouldHave(text(order.label()));
        snippets().shouldHave(sizeGreaterThan(0));
        return this;
    }

    /** Выдача перерисовывается асинхронно после смены URL — ждём, но окончательную проверку делает тест. */
    private void waitResultsUpdated(java.util.function.BooleanSupplier condition) {
        Waits.until(condition);
    }

    public String currentUrl() {
        return WebDriverRunner.url();
    }
}

package ru.yandex.market.tests.screenshot;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.market.annotations.DesktopOnly;
import ru.yandex.market.annotations.ScreenshotTest;
import ru.yandex.market.pages.MainPage;
import ru.yandex.market.pages.SearchResultsPage;
import ru.yandex.market.screenshot.VisualCheck;
import ru.yandex.market.tests.BaseTest;

@ScreenshotTest
@DesktopOnly
@Feature("Скриншоты")
@DisplayName("Скриншоты desktop")
class DesktopScreenshotTest extends BaseTest {

    @Test
    @DisplayName("Строка поиска на главной")
    void searchRow() {
        MainPage page = new MainPage().open();
        VisualCheck.of("main-search-row", page.header().searchRow()).verify();
    }

    @Test
    @DisplayName("Строка поиска с введённым запросом")
    void searchRowWithQuery() {
        MainPage page = new MainPage().open();
        page.header().searchInput().setValue("iphone 16");
        VisualCheck.of("main-search-row-filled", page.header().searchRow())
                // подсказки поиска открываются поверх и зависят от популярности запросов
                .hide("[data-zone-name=suggestBlock]")
                .verify();
    }

    @Test
    @DisplayName("Контрол сортировки в выдаче")
    void sortControl() {
        SearchResultsPage results = new SearchResultsPage().open("iphone");
        VisualCheck.of("search-sort-control", results.sortControl()).verify();
    }

    @Test
    @DisplayName("Фильтр по цене (границы цен скрыты)")
    void priceFilter() {
        SearchResultsPage results = new SearchResultsPage().open("iphone");
        VisualCheck.of("search-price-filter", results.priceFilter())
                // подписи «От 21 ₽ / До 485 915 ₽» зависят от текущих цен
                .hide("[data-auto=filter-range-glprice] label")
                .verify();
    }

    @Test
    @DisplayName("Подвал сайта")
    void footer() {
        MainPage page = new MainPage().open();
        VisualCheck.of("main-footer", page.footer())
                .hide("[data-auto=qrCodePicture]")
                .tolerance(1.0)
                .verify();
    }
}

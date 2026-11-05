package ru.yandex.market.tests.ui;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.market.annotations.DesktopOnly;
import ru.yandex.market.annotations.UiTest;
import ru.yandex.market.pages.MainPage;
import ru.yandex.market.pages.SearchResultsPage;
import ru.yandex.market.pages.SortOrder;
import ru.yandex.market.tests.BaseTest;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@Feature("Поиск")
@DisplayName("Поиск товаров")
class SearchTest extends BaseTest {

    @ParameterizedTest(name = "Запрос «{0}»")
    @ValueSource(strings = {"iphone", "ноутбук"})
    @Tag("smoke")
    @DisplayName("Поиск с главной находит релевантные товары")
    void searchFromMainPage(String query) {
        SearchResultsPage results = new MainPage().open()
                .header().search(query);

        assertThat(results.currentUrl()).contains("text=");
        List<String> titles = results.titles(5);
        assertThat(titles).isNotEmpty();
        assertThat(titles)
                .as("Хотя бы один из первых товаров содержит «%s»", query)
                .anyMatch(t -> t.toLowerCase(Locale.ROOT).contains(query));
    }

    @Test
    @DesktopOnly
    @DisplayName("Фильтр «Цена от» оставляет товары не дешевле заданной суммы")
    void priceFromFilter() {
        int min = 50_000;
        SearchResultsPage results = new SearchResultsPage().open("iphone")
                .setPriceFrom(min);

        assertThat(results.prices(10))
                .isNotEmpty()
                .allSatisfy(price -> assertThat(price).isGreaterThanOrEqualTo(min));
    }

    @Test
    @DesktopOnly
    @DisplayName("Сортировка «Подешевле» упорядочивает выдачу по возрастанию цены")
    void sortByPriceAscending() {
        SearchResultsPage results = new SearchResultsPage().open("iphone")
                .sortBy(SortOrder.CHEAPER);

        assertThat(results.prices(10)).isNotEmpty().isSorted();
    }
}

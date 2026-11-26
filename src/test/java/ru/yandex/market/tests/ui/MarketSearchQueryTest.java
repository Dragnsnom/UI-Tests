package ru.yandex.market.tests.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.market.annotations.DesktopOnly;
import ru.yandex.market.annotations.UiTest;
import ru.yandex.market.pages.MainPage;
import ru.yandex.market.tests.BaseTest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.Wait;
import static org.openqa.selenium.support.ui.ExpectedConditions.urlMatches;

@UiTest
@DesktopOnly
class MarketSearchQueryTest extends BaseTest {

    @Test
    @DisplayName("Строка поиска отправляет запрос «ноутбук»")
    void searchBarSubmitsQuery() {
        String query = "ноутбук";
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        new MainPage().open().header().submitSearch(query);

        Wait().until(urlMatches(
                ".*/search\\?(?:[^#]*&)?text=" + Pattern.quote(encodedQuery) + "(?:[&#].*)?$"));
    }
}

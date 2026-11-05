package ru.yandex.market.tests.screenshot;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.market.annotations.MobileOnly;
import ru.yandex.market.annotations.ScreenshotTest;
import ru.yandex.market.pages.MainPage;
import ru.yandex.market.screenshot.VisualCheck;
import ru.yandex.market.tests.BaseTest;

@ScreenshotTest
@MobileOnly
@Feature("Скриншоты")
@DisplayName("Скриншоты mobile")
class MobileScreenshotTest extends BaseTest {

    @Test
    @DisplayName("Строка поиска на главной")
    void searchRow() {
        MainPage page = new MainPage().open();
        VisualCheck.of("main-search-row", page.header().searchRow()).verify();
    }

    @Test
    @DisplayName("Нижний таббар")
    void tabBar() {
        MainPage page = new MainPage().open();
        VisualCheck.of("main-tabbar", page.header().tabBar()).verify();
    }
}

package ru.yandex.market.tests.ui;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.yandex.market.annotations.DesktopOnly;
import ru.yandex.market.annotations.UiTest;
import ru.yandex.market.pages.MainPage;
import ru.yandex.market.pages.SearchResultsPage;
import ru.yandex.market.tests.BaseTest;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.title;
import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@Feature("Главная страница")
@DisplayName("Главная страница")
class MainPageTest extends BaseTest {

    @Test
    @Tag("smoke")
    @DisplayName("Открывается, есть поиск и лента товаров")
    void mainPageOpens() {
        MainPage page = new MainPage().open();

        assertThat(title()).contains("Яндекс Маркет");
        page.header().searchInput().shouldBe(visible).shouldHave(attribute("placeholder", "Найти товары"));
        page.feedSnippets().shouldHave(sizeGreaterThan(0));
    }

    @Test
    @DesktopOnly
    @DisplayName("Логотип ведёт на главную")
    void logoLeadsToMainPage() {
        new SearchResultsPage().open("iphone")
                .header().clickLogo();

        assertThat(WebDriverRunner.url()).doesNotContain("/search");
    }
}

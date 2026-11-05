package ru.yandex.market.extensions;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import ru.yandex.market.config.TestConfig;
import ru.yandex.market.driver.DriverConfigurator;

/**
 * Жизненный цикл браузера: настройка Selenide перед классом, отдельный браузер на каждый тест
 * (чистые cookies, корзина и т. п.), распознавание капчи при падении.
 */
public class BrowserExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, TestExecutionExceptionHandler {

    @Override
    public void beforeAll(ExtensionContext context) {
        DriverConfigurator.configure();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        Allure.parameter("profile", TestConfig.get().profile());
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        if (!(throwable instanceof SiteBlockedException)) {
            SiteGuard.checkNotBlocked(throwable);
        }
        throw throwable;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Selenide.closeWebDriver();
    }
}

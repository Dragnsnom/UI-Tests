package ru.yandex.market.tests;

import org.junit.jupiter.api.extension.ExtendWith;
import ru.yandex.market.config.TestConfig;
import ru.yandex.market.extensions.BrowserExtension;
import ru.yandex.market.extensions.PlatformCondition;

/** Базовый класс тестов: подключает жизненный цикл браузера и фильтр по режиму desktop/mobile. */
@ExtendWith({PlatformCondition.class, BrowserExtension.class})
public abstract class BaseTest {

    protected final TestConfig config = TestConfig.get();
}

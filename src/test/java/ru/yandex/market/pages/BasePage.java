package ru.yandex.market.pages;

import com.codeborne.selenide.Selenide;
import ru.yandex.market.extensions.SiteGuard;
import ru.yandex.market.pages.components.Header;
import ru.yandex.market.pages.components.Popups;

/** Общее для всех страниц: шапка, открытие по пути, проверка на капчу, закрытие попапов. */
public abstract class BasePage<T extends BasePage<T>> {

    protected final Header header = new Header();

    public Header header() {
        return header;
    }

    /** Ожидание ключевого элемента страницы. */
    protected abstract void waitForPageElements();

    @SuppressWarnings("unchecked")
    public T waitLoaded() {
        SiteGuard.checkNotBlocked();
        waitForPageElements();
        Popups.dismiss();
        return (T) this;
    }

    protected T openPath(String path) {
        Selenide.open(path);
        return waitLoaded();
    }
}

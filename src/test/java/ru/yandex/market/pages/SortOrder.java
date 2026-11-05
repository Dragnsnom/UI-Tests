package ru.yandex.market.pages;

/** Варианты сортировки выдачи (подписи и значения параметра how в URL). */
public enum SortOrder {
    POPULAR("Популярные", null),
    CHEAPER("Подешевле", "aprice"),
    EXPENSIVE("Подороже", "dprice"),
    RATING("Высокий рейтинг", null);

    private final String label;
    private final String urlValue;

    SortOrder(String label, String urlValue) {
        this.label = label;
        this.urlValue = urlValue;
    }

    public String label() {
        return label;
    }

    /** Значение how=..., если оно известно. */
    public String urlValue() {
        return urlValue;
    }
}

package ru.yandex.market.config;

import java.util.concurrent.ThreadLocalRandom;

/** Диапазон задержки в мс: "300-900" или одно число "500". "0" отключает паузу. */
public record DelayRange(long min, long max) {

    public static DelayRange parse(String value) {
        String[] parts = value.split("-", 2);
        long min = Long.parseLong(parts[0].trim());
        long max = parts.length > 1 ? Long.parseLong(parts[1].trim()) : min;
        if (min < 0 || max < min) {
            throw new IllegalArgumentException("Некорректный диапазон задержки: " + value);
        }
        return new DelayRange(min, max);
    }

    public long random() {
        return min == max ? min : ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    @Override
    public String toString() {
        return min == max ? String.valueOf(min) : min + "-" + max;
    }
}

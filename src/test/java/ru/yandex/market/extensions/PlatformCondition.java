package ru.yandex.market.extensions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import ru.yandex.market.annotations.DesktopOnly;
import ru.yandex.market.annotations.MobileOnly;
import ru.yandex.market.config.Mode;
import ru.yandex.market.config.TestConfig;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

/** Пропускает тесты, помеченные {@link DesktopOnly}/{@link MobileOnly}, если режим прогона не совпадает. */
public class PlatformCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Mode current = TestConfig.get().mode();
        Optional<AnnotatedElement> element = context.getElement();
        if (AnnotationSupport.isAnnotated(element, DesktopOnly.class) && current != Mode.DESKTOP) {
            return ConditionEvaluationResult.disabled("Тест только для desktop, текущий режим: " + current.id());
        }
        if (AnnotationSupport.isAnnotated(element, MobileOnly.class) && current != Mode.MOBILE) {
            return ConditionEvaluationResult.disabled("Тест только для mobile, текущий режим: " + current.id());
        }
        return ConditionEvaluationResult.enabled("Режим " + current.id());
    }
}

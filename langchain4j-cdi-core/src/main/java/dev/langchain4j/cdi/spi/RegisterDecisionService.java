package dev.langchain4j.cdi.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.langchain4j.model.decision.listener.DecisionModelListener;
import dev.langchain4j.service.decision.DecisionServices;
import dev.langchain4j.service.decision.ThresholdProvider;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Stereotype;

/**
 * Stereotype to register an interface as a LangChain4j Decision Service.
 *
 * <p>Apply it on an interface that will be implemented dynamically by LangChain4j {@link DecisionServices}. You can optionally
 * reference named CDI beans to wire the service: models. If a property name is blank,
 * the dependency is ignored. For decisionModelName, "#default" means select the default bean.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterDecisionService {

    /**
     * CDI scope for the AI service bean.
     *
     * @return the scope annotation class
     */
    Class<? extends Annotation> scope() default RequestScoped.class;

    /**
     * CDI bean name of the decision model. Use "#default" for the default bean, or blank to skip.
     *
     * @return the chat model bean name
     */
    String decisionModelName() default "#default";

    /**
     * CDI bean name of the {@link ThresholdProvider}. Blank means no streaming model is wired.
     *
     * @return the streaming chat model bean name
     */
    String thresholdProviderName() default "";

    /**
     * Named CDI beans implementing {@link DecisionModelListener} to register on
     * the Decision service. Each listener receives lifecycle events (request issued, response received, errors, etc.) for
     * this service only. Unresolvable names are skipped with a WARNING log.
     *
     * @return the listener bean names
     */
    String[] listenerNames() default {};
}

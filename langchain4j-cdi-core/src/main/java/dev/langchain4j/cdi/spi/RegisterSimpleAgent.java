package dev.langchain4j.cdi.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Stereotype to register an interface as a LangChain4j SIMPLE agent backed by a plain AI service.
 *
 * <p>All CDI dependencies are resolved exclusively by name. Use {@code "#default"} to select the default bean of a
 * type, leave blank to skip, or provide an explicit bean name (or an expression resolved via
 * {@code ExpressionResolver}).
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterSimpleAgent {

    Class<? extends Annotation> scope() default ApplicationScoped.class;

    String name() default "";

    String description() default "";

    String outputKey() default "";

    boolean async() default false;

    String agentListenerName() default "";

    String chatModelName() default "#default";

    String streamingChatModelName() default "";

    /**
     * CDI bean names of tool objects to register. Each name must resolve to a {@code @Named} CDI bean. Ignored when
     * {@link #toolProviderName()} is set.
     */
    String[] toolNames() default {};

    String toolProviderName() default "";

    String chatMemoryName() default "";

    String chatMemoryProviderName() default "";

    String contentRetrieverName() default "";

    String retrievalAugmentorName() default "";

    String[] inputGuardrailNames() default {};

    String[] outputGuardrailNames() default {};
}

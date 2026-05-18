package dev.langchain4j.cdi.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Stereotype to register an interface as a LangChain4j HUMAN_IN_THE_LOOP agent that pauses an agentic workflow for
 * human input.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterHumanInTheLoopAgent {

    Class<? extends Annotation> scope() default ApplicationScoped.class;

    String name() default "";

    String description() default "";

    String outputKey() default "";

    boolean async() default false;

    String agentListenerName() default "";

    /** CDI bean name of a {@code Supplier<?>} or {@code Function<AgenticScope, ?>} that provides the human response. */
    String responseProviderName() default "";
}

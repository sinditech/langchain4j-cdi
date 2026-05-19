package dev.langchain4j.cdi.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Stereotype to register an interface as a LangChain4j PARALLEL_MAPPER agent that applies a single sub-agent to each
 * item in a list read from the {@link dev.langchain4j.agentic.scope.AgenticScope}.
 *
 * <p>{@link #itemsKey()} is required and must name the scope key holding the list of items to map over. The first entry
 * in {@link #subAgentNames()} is the worker agent applied to each item.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterParallelMapperAgent {

    Class<? extends Annotation> scope() default ApplicationScoped.class;

    String name() default "";

    String description() default "";

    String outputKey() default "";

    String agentListenerName() default "";

    String[] subAgentNames() default {};

    /** Key in {@link dev.langchain4j.agentic.scope.AgenticScope} that holds the list of items to map over. Required. */
    String itemsKey();
}

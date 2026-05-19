package dev.langchain4j.cdi.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Stereotype to register an interface as a LangChain4j A2A (Agent-to-Agent) agent that delegates to a remote A2A
 * server.
 *
 * <p>{@link #a2aServerUrl()} is required. Add the {@code langchain4j-cdi-a2a} module to the classpath to activate the
 * A2A protocol implementation.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterA2AAgent {

    Class<? extends Annotation> scope() default ApplicationScoped.class;

    String name() default "";

    String description() default "";

    String outputKey() default "";

    boolean async() default false;

    String agentListenerName() default "";

    /** URL of the A2A server. Required. */
    String a2aServerUrl();
}

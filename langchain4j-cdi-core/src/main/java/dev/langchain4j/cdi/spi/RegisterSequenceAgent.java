package dev.langchain4j.cdi.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Stereotype to register an interface as a LangChain4j SEQUENCE agent that chains sub-agents in order. */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterSequenceAgent {

    Class<? extends Annotation> scope() default ApplicationScoped.class;

    String name() default "";

    String description() default "";

    String outputKey() default "";

    /**
     * If true, the agent's execution will be silently skipped when any of its arguments is missing in the agentic
     * scope, instead of making the agentic system's execution fail.
     */
    boolean optional() default false;

    /** Names of other agents whose conversation context should be summarized and injected into this agent's prompt. */
    String[] summarizedContext() default {};

    String agentListenerName() default "";

    String[] subAgentNames() default {};
}

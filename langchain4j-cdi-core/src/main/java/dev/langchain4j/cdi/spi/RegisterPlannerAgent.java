package dev.langchain4j.cdi.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Stereotype to register an interface as a LangChain4j PLANNER agent.
 *
 * <p>Either {@link #plannerName()} must resolve to a CDI {@link dev.langchain4j.agentic.planner.Planner} bean, or the
 * interface must declare a static method annotated with
 * {@link dev.langchain4j.agentic.declarative.PlannerSupplier @PlannerSupplier}.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterPlannerAgent {

    Class<? extends Annotation> scope() default ApplicationScoped.class;

    String name() default "";

    String description() default "";

    String outputKey() default "";

    String agentListenerName() default "";

    String[] subAgentNames() default {};

    String plannerName() default "";
}

package dev.langchain4j.cdi.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Stereotype to register an interface as a LangChain4j LOOP agent that iterates sub-agents repeatedly. */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterLoopAgent {

    Class<? extends Annotation> scope() default ApplicationScoped.class;

    String name() default "";

    String description() default "";

    String outputKey() default "";

    String agentListenerName() default "";

    String[] subAgentNames() default {};

    int maxIterations() default 10;

    /**
     * CDI bean name of a {@code Predicate<AgenticScope>} or {@code BiPredicate<AgenticScope, Integer>} that controls
     * early exit from the loop. Blank means no early exit condition is applied.
     */
    String exitConditionName() default "";

    String exitConditionDescription() default "";

    boolean testAfterEachIteration() default false;
}

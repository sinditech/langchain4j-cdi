package dev.langchain4j.cdi.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Stereotype to register an interface as a LangChain4j SUPERVISOR agent that orchestrates sub-agents via an LLM. */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterSupervisorAgent {

    Class<? extends Annotation> scope() default ApplicationScoped.class;

    String name() default "";

    String description() default "";

    String outputKey() default "";

    String agentListenerName() default "";

    String[] subAgentNames() default {};

    String chatModelName() default "#default";

    String chatMemoryProviderName() default "";

    int maxAgentsInvocations() default 10;

    String supervisorContext() default "";

    SupervisorContextStrategy supervisorContextStrategy() default SupervisorContextStrategy.CHAT_MEMORY;

    SupervisorResponseStrategy supervisorResponseStrategy() default SupervisorResponseStrategy.LAST;
}

package dev.langchain4j.cdi.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import dev.langchain4j.cdi.agent.AgentTopologyType;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrail;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Stereotype to register an interface as a LangChain4j Agent.
 *
 * <p>Apply it on an interface that will be implemented dynamically by the agentic framework. The annotation carries
 * both CDI wiring (named beans for models, tools, memory, etc.) and agent orchestration metadata (topology, sub-agents,
 * output key). Named CDI bean resolution follows the same conventions as {@link RegisterAIService}: {@code "#default"}
 * selects the default bean, blank means ignore, and a specific name resolves via {@code @Named}.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterAgent {

    Class<? extends Annotation> scope() default ApplicationScoped.class;

    String name() default "";

    String description() default "";

    AgentTopologyType topology() default AgentTopologyType.SIMPLE;

    String outputKey() default "";

    boolean async() default false;

    /**
     * CDI bean names of sub-agents for composed topologies (SEQUENCE, LOOP, PARALLEL, CONDITIONAL, SUPERVISOR,
     * PLANNER). Each name must match a {@code @Named} CDI bean implementing the sub-agent interface.
     */
    String[] subAgentNames() default {};

    int maxIterations() default 10;

    int maxAgentsInvocations() default 10;

    String chatModelName() default "#default";

    String streamingChatModelName() default "";

    Class<?>[] tools() default {};

    String toolProviderName() default "";

    String chatMemoryName() default "";

    String chatMemoryProviderName() default "";

    String contentRetrieverName() default "";

    String retrievalAugmentorName() default "";

    /**
     * Input guardrail classes to validate messages before sending to the LLM. If a class is a CDI managed bean, the
     * bean instance is used; otherwise it is instantiated via its no-arg constructor. Mutually exclusive with
     * {@link #inputGuardrailNames()}: if both are specified, only the classes are used and the names are ignored.
     */
    Class<? extends InputGuardrail>[] inputGuardrails() default {};

    /**
     * Output guardrail classes to validate LLM responses before returning them. If a class is a CDI managed bean, the
     * bean instance is used; otherwise it is instantiated via its no-arg constructor. Mutually exclusive with
     * {@link #outputGuardrailNames()}: if both are specified, only the classes are used and the names are ignored.
     */
    Class<? extends OutputGuardrail>[] outputGuardrails() default {};

    /**
     * Named CDI beans implementing {@link InputGuardrail} to validate messages before sending to the LLM. Unresolvable
     * names are skipped with a WARNING log. Mutually exclusive with {@link #inputGuardrails()}: if both are specified,
     * only the classes are used and the names are ignored.
     */
    String[] inputGuardrailNames() default {};

    /**
     * Named CDI beans implementing {@link OutputGuardrail} to validate LLM responses before returning them.
     * Unresolvable names are skipped with a WARNING log. Mutually exclusive with {@link #outputGuardrails()}: if both
     * are specified, only the classes are used and the names are ignored.
     */
    String[] outputGuardrailNames() default {};

    String agentListenerName() default "";

    /**
     * URL of the A2A server for A2A topology agents. Only used when {@link #topology()} is
     * {@link AgentTopologyType#A2A}.
     */
    String a2aServerUrl() default "";
}

package dev.langchain4j.cdi.agent;

import dev.langchain4j.cdi.aiservice.CdiLookupHelper;
import dev.langchain4j.cdi.spi.RegisterA2AAgent;
import dev.langchain4j.cdi.spi.RegisterConditionalAgent;
import dev.langchain4j.cdi.spi.RegisterHumanInTheLoopAgent;
import dev.langchain4j.cdi.spi.RegisterLoopAgent;
import dev.langchain4j.cdi.spi.RegisterMcpClientAgent;
import dev.langchain4j.cdi.spi.RegisterParallelAgent;
import dev.langchain4j.cdi.spi.RegisterParallelMapperAgent;
import dev.langchain4j.cdi.spi.RegisterPlannerAgent;
import dev.langchain4j.cdi.spi.RegisterSequenceAgent;
import dev.langchain4j.cdi.spi.RegisterSimpleAgent;
import dev.langchain4j.cdi.spi.RegisterSupervisorAgent;
import java.lang.annotation.Annotation;

/**
 * Extracts CDI metadata from any of the 11 agent stereotype annotations: {@link RegisterSimpleAgent},
 * {@link RegisterSequenceAgent}, {@link RegisterLoopAgent}, {@link RegisterParallelAgent},
 * {@link RegisterParallelMapperAgent}, {@link RegisterConditionalAgent}, {@link RegisterSupervisorAgent},
 * {@link RegisterPlannerAgent}, {@link RegisterA2AAgent}, {@link RegisterMcpClientAgent}, and
 * {@link RegisterHumanInTheLoopAgent}.
 *
 * <p>Use {@link #detect(Class)} to obtain an instance from an interface class, or {@code null} when no recognized agent
 * annotation is present. Raw string fields ({@link #rawName()}, {@link #rawDescription()}, {@link #rawOutputKey()})
 * contain the annotation values before expression resolution; use {@link #name()}, {@link #description()}, and
 * {@link #outputKey()} to get the resolved values.
 *
 * <p><b>API note:</b> this record is part of the stable public API of {@code langchain4j-cdi-core}, alongside the
 * annotations in {@code dev.langchain4j.cdi.spi}. It lives in the {@code dev.langchain4j.cdi.agent} package for
 * historical reasons; treat it as public and stable.
 */
public record AgentAnnotationMeta(
        Class<? extends Annotation> scope,
        String rawName,
        String rawDescription,
        String rawOutputKey,
        boolean async,
        Class<? extends Annotation> annotationClass) {

    /**
     * Detects which agent stereotype annotation is present on {@code interfaceClass} and returns the corresponding
     * meta. Returns {@code null} when no recognized annotation is found.
     *
     * <p><b>Sync note:</b> when adding a new topology annotation, update all four of:
     *
     * <ol>
     *   <li>this method
     *   <li>{@link CommonAgentCreator#create} dispatch chain
     *   <li>{@code hasAnyAgentAnnotation} in the build-compatible extension
     *   <li>the {@code @Enhancement}/{@code @WithAnnotations} arrays in the build-compatible extension
     * </ol>
     */
    public static AgentAnnotationMeta detect(Class<?> interfaceClass) {
        if (!interfaceClass.isInterface()) return null;
        RegisterSimpleAgent simple = interfaceClass.getAnnotation(RegisterSimpleAgent.class);
        if (simple != null)
            return new AgentAnnotationMeta(
                    simple.scope(),
                    simple.name(),
                    simple.description(),
                    simple.outputKey(),
                    simple.async(),
                    RegisterSimpleAgent.class);

        RegisterSequenceAgent seq = interfaceClass.getAnnotation(RegisterSequenceAgent.class);
        if (seq != null)
            return new AgentAnnotationMeta(
                    seq.scope(), seq.name(), seq.description(), seq.outputKey(), false, RegisterSequenceAgent.class);

        RegisterLoopAgent loop = interfaceClass.getAnnotation(RegisterLoopAgent.class);
        if (loop != null)
            return new AgentAnnotationMeta(
                    loop.scope(), loop.name(), loop.description(), loop.outputKey(), false, RegisterLoopAgent.class);

        RegisterParallelAgent parallel = interfaceClass.getAnnotation(RegisterParallelAgent.class);
        if (parallel != null)
            return new AgentAnnotationMeta(
                    parallel.scope(),
                    parallel.name(),
                    parallel.description(),
                    parallel.outputKey(),
                    false,
                    RegisterParallelAgent.class);

        RegisterParallelMapperAgent mapper = interfaceClass.getAnnotation(RegisterParallelMapperAgent.class);
        if (mapper != null)
            return new AgentAnnotationMeta(
                    mapper.scope(),
                    mapper.name(),
                    mapper.description(),
                    mapper.outputKey(),
                    false,
                    RegisterParallelMapperAgent.class);

        RegisterConditionalAgent cond = interfaceClass.getAnnotation(RegisterConditionalAgent.class);
        if (cond != null)
            return new AgentAnnotationMeta(
                    cond.scope(),
                    cond.name(),
                    cond.description(),
                    cond.outputKey(),
                    false,
                    RegisterConditionalAgent.class);

        RegisterSupervisorAgent supervisor = interfaceClass.getAnnotation(RegisterSupervisorAgent.class);
        if (supervisor != null)
            return new AgentAnnotationMeta(
                    supervisor.scope(),
                    supervisor.name(),
                    supervisor.description(),
                    supervisor.outputKey(),
                    false,
                    RegisterSupervisorAgent.class);

        RegisterPlannerAgent planner = interfaceClass.getAnnotation(RegisterPlannerAgent.class);
        if (planner != null)
            return new AgentAnnotationMeta(
                    planner.scope(),
                    planner.name(),
                    planner.description(),
                    planner.outputKey(),
                    false,
                    RegisterPlannerAgent.class);

        RegisterA2AAgent a2a = interfaceClass.getAnnotation(RegisterA2AAgent.class);
        if (a2a != null)
            return new AgentAnnotationMeta(
                    a2a.scope(), a2a.name(), a2a.description(), a2a.outputKey(), a2a.async(), RegisterA2AAgent.class);

        RegisterMcpClientAgent mcp = interfaceClass.getAnnotation(RegisterMcpClientAgent.class);
        if (mcp != null)
            return new AgentAnnotationMeta(
                    mcp.scope(),
                    mcp.name(),
                    mcp.description(),
                    mcp.outputKey(),
                    mcp.async(),
                    RegisterMcpClientAgent.class);

        RegisterHumanInTheLoopAgent hitl = interfaceClass.getAnnotation(RegisterHumanInTheLoopAgent.class);
        if (hitl != null)
            return new AgentAnnotationMeta(
                    hitl.scope(),
                    hitl.name(),
                    hitl.description(),
                    hitl.outputKey(),
                    hitl.async(),
                    RegisterHumanInTheLoopAgent.class);

        return null;
    }

    /** Returns {@code true} when any recognized agent annotation is present on {@code interfaceClass}. */
    public static boolean isAgentInterface(Class<?> interfaceClass) {
        return detect(interfaceClass) != null;
    }

    /** Expression-resolved name, equivalent to the annotation's {@code name()} after EL/Config expansion. */
    public String name() {
        return CdiLookupHelper.resolveExpression(rawName);
    }

    /**
     * Expression-resolved description, equivalent to the annotation's {@code description()} after EL/Config expansion.
     */
    public String description() {
        return CdiLookupHelper.resolveExpression(rawDescription);
    }

    /** Expression-resolved output key, equivalent to the annotation's {@code outputKey()} after EL/Config expansion. */
    public String outputKey() {
        return CdiLookupHelper.resolveExpression(rawOutputKey);
    }
}

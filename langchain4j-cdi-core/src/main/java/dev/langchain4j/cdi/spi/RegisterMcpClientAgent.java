package dev.langchain4j.cdi.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Stereotype to register an interface as a LangChain4j MCP_CLIENT agent that wraps an MCP server tool.
 *
 * <p>Both {@link #mcpClientName()} and {@link #mcpToolName()} are required.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterMcpClientAgent {

    Class<? extends Annotation> scope() default ApplicationScoped.class;

    String name() default "";

    String description() default "";

    String outputKey() default "";

    boolean async() default false;

    String agentListenerName() default "";

    /** CDI bean name of the MCP client object. Required. */
    String mcpClientName();

    /** Name of the MCP server tool to invoke. Required. */
    String mcpToolName();

    /** Keys of the input arguments read from {@link dev.langchain4j.agentic.scope.AgenticScope}. */
    String[] mcpInputKeys() default {};
}

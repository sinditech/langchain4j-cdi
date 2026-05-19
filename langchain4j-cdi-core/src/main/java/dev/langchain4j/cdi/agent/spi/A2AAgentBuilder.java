package dev.langchain4j.cdi.agent.spi;

import jakarta.enterprise.inject.Instance;

/**
 * SPI for building A2A (Agent-to-Agent) agents. Implemented by the {@code langchain4j-cdi-a2a} module which brings in
 * the required {@code langchain4j-agentic-a2a} dependency.
 */
public interface A2AAgentBuilder {

    /** Build an A2A agent from individual field values. */
    <X> X build(
            Class<X> interfaceClass,
            String url,
            String outputKey,
            boolean async,
            String agentListenerName,
            Instance<Object> lookup);
}

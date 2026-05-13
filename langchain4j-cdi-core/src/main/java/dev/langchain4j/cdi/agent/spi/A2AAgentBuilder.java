package dev.langchain4j.cdi.agent.spi;

import dev.langchain4j.cdi.spi.RegisterAgent;
import jakarta.enterprise.inject.Instance;

/**
 * SPI for building A2A (Agent-to-Agent) agents from {@link RegisterAgent} metadata. Implemented by the
 * {@code langchain4j-cdi-a2a} module which brings in the required {@code langchain4j-agentic-a2a} dependency.
 */
public interface A2AAgentBuilder {

    <X> X build(Class<X> interfaceClass, RegisterAgent annotation, Instance<Object> lookup);
}

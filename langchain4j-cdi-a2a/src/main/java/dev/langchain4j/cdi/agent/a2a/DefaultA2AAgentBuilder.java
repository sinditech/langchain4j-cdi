package dev.langchain4j.cdi.agent.a2a;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.internal.A2AClientBuilder;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.cdi.agent.spi.A2AAgentBuilder;
import dev.langchain4j.cdi.aiservice.CdiLookupHelper;
import dev.langchain4j.cdi.spi.RegisterAgent;
import jakarta.enterprise.inject.Instance;

public class DefaultA2AAgentBuilder implements A2AAgentBuilder {

    @Override
    public <X> X build(Class<X> interfaceClass, RegisterAgent annotation, Instance<Object> lookup) {
        String url = CdiLookupHelper.resolveExpression(annotation.a2aServerUrl());
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "A2A topology requires a non-empty a2aServerUrl on @RegisterAgent for " + interfaceClass.getName());
        }
        A2AClientBuilder<X> builder = AgenticServices.a2aBuilder(url, interfaceClass);
        String outputKey = CdiLookupHelper.resolveExpression(annotation.outputKey());
        if (outputKey != null && !outputKey.isBlank()) {
            builder.outputKey(outputKey);
        }
        builder.async(annotation.async());
        AgentListener listener =
                CdiLookupHelper.resolveSingle(lookup, AgentListener.class, annotation.agentListenerName());
        if (listener != null) {
            builder.listener(listener);
        }
        return builder.build();
    }
}

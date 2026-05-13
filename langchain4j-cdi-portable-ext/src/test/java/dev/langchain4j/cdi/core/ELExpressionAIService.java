package dev.langchain4j.cdi.core;

import dev.langchain4j.cdi.spi.RegisterAIService;
import jakarta.enterprise.context.ApplicationScoped;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAIService(chatModelName = "#{true ? 'chat-model-dummy' : 'other'}", scope = ApplicationScoped.class)
public interface ELExpressionAIService {
    String chat(String question);
}

package dev.langchain4j.cdi.core;

import dev.langchain4j.cdi.spi.RegisterAIService;
import jakarta.enterprise.context.ApplicationScoped;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAIService(chatModelName = "${test.expression.model.name}", scope = ApplicationScoped.class)
public interface MpConfigExpressionAIService {
    String chat(String question);
}

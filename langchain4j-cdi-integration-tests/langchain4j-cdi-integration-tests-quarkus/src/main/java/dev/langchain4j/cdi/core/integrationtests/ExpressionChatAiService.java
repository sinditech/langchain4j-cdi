package dev.langchain4j.cdi.core.integrationtests;

import dev.langchain4j.cdi.spi.RegisterAIService;
import dev.langchain4j.service.SystemMessage;
import jakarta.enterprise.context.ApplicationScoped;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAIService(chatModelName = "${test.expression.model}", scope = ApplicationScoped.class)
public interface ExpressionChatAiService {

    @SystemMessage("my system message.")
    String chat(String question);
}

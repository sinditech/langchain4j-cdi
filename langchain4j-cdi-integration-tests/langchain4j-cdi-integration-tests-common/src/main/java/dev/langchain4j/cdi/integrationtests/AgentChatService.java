package dev.langchain4j.cdi.integrationtests;

import dev.langchain4j.cdi.spi.RegisterAgent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import jakarta.enterprise.context.ApplicationScoped;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAgent(chatModelName = "chat-model", scope = ApplicationScoped.class)
public interface AgentChatService {

    @SystemMessage("You are a helpful assistant.")
    @UserMessage("{question}")
    String chat(@V("question") String question);
}

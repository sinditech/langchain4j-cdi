package dev.langchain4j.cdi.core;

import dev.langchain4j.cdi.spi.RegisterSimpleAgent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterSimpleAgent(chatModelName = "#default")
public interface MyDummyAgent {
    @UserMessage("{question}")
    String chat(@V("question") String question);
}

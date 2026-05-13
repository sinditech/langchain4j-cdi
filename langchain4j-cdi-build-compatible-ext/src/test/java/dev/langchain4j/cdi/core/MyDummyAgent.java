package dev.langchain4j.cdi.core;

import dev.langchain4j.cdi.spi.RegisterAgent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAgent
public interface MyDummyAgent {
    @UserMessage("{question}")
    String chat(@V("question") String question);
}

package dev.langchain4j.cdi.core;

import dev.langchain4j.cdi.spi.RegisterAgent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import jakarta.enterprise.context.ApplicationScoped;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAgent(name = "namedAgent", scope = ApplicationScoped.class)
public interface MyDummyNamedAgent {
    @UserMessage("{input}")
    String respond(@V("input") String input);
}

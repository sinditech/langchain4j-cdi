package dev.langchain4j.cdi.core;

import dev.langchain4j.cdi.spi.RegisterSimpleAgent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import jakarta.enterprise.context.ApplicationScoped;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterSimpleAgent(name = "namedAgent", scope = ApplicationScoped.class, chatModelName = "#default")
public interface MyDummyNamedAgent {
    @UserMessage("{input}")
    String respond(@V("input") String input);
}

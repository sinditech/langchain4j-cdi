package dev.langchain4j.cdi.integrationtests;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/agent-chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentChatRestService {

    @Inject
    AgentChatService agentChatService;

    @POST
    public String postChat(String chatRequest) {
        return agentChatService.chat(chatRequest);
    }
}

package dev.langchain4j.cdi.core.integrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

/**
 * Integration tests verifying that agent beans are correctly created and invokable through the build-compatible
 * extension path used by Quarkus.
 */
@QuarkusTest
public class QuarkusAgentIntegrationTest {

    @ConfigProperty(name = "quarkus.http.test-port")
    int port;

    @Test
    public void testAgentChatRestService() {
        String agentEndpoint = "http://localhost:" + port + "/agent-chat";
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(agentEndpoint);

            String question = "What is the meaning of life?";
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(question, MediaType.APPLICATION_JSON));

            assertThat(response.getStatus()).isEqualTo(200);
            String result = response.readEntity(String.class);
            assertThat(result).isNotNull().isEqualTo("ok");
        }
    }
}

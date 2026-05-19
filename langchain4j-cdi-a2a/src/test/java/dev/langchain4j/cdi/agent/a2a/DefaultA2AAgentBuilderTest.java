package dev.langchain4j.cdi.agent.a2a;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.internal.A2AClientBuilder;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.cdi.spi.RegisterA2AAgent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class DefaultA2AAgentBuilderTest {

    interface TestAgent {
        String chat(String input);
    }

    interface TestAgentWithOutputKey {
        String process(String input);
    }

    interface TestAsyncAgent {
        String process(String input);
    }

    interface TestAgentWithListener {
        String chat(String input);
    }

    @SuppressWarnings("CdiManagedBeanInconsistencyInspection")
    @RegisterA2AAgent(a2aServerUrl = "")
    interface TestAgentBlankUrl {
        String chat(String input);
    }

    @SuppressWarnings("unchecked")
    private static <T> A2AClientBuilder<T> stubA2ABuilder(T proxy) {
        A2AClientBuilder<T> builder = mock(A2AClientBuilder.class);
        when(builder.build()).thenReturn(proxy);
        return builder;
    }

    @Test
    void build_withBlankUrl_throwsIllegalArgumentException() {
        DefaultA2AAgentBuilder subject = new DefaultA2AAgentBuilder();
        @SuppressWarnings("unchecked")
        Instance<Object> lookup = mock(Instance.class);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> subject.build(TestAgentBlankUrl.class, "", "", false, "", lookup));
        assertTrue(ex.getMessage().contains("a2aServerUrl"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void build_withValidUrl_buildsAndReturnsProxy() {
        DefaultA2AAgentBuilder subject = new DefaultA2AAgentBuilder();
        Instance<Object> lookup = mock(Instance.class);
        TestAgent mockProxy = mock(TestAgent.class);
        A2AClientBuilder<TestAgent> a2aBuilder = stubA2ABuilder(mockProxy);

        try (MockedStatic<AgenticServices> agSvc = mockStatic(AgenticServices.class)) {
            agSvc.when(() -> AgenticServices.a2aBuilder("http://agent.example.com", TestAgent.class))
                    .thenReturn(a2aBuilder);

            TestAgent result = subject.build(TestAgent.class, "http://agent.example.com", "", false, "", lookup);

            assertSame(mockProxy, result);
            verify(a2aBuilder).async(false);
            verify(a2aBuilder).build();
            verify(a2aBuilder, never()).outputKey(anyString());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void build_withOutputKey_setsOutputKeyOnBuilder() {
        DefaultA2AAgentBuilder subject = new DefaultA2AAgentBuilder();
        Instance<Object> lookup = mock(Instance.class);
        TestAgentWithOutputKey mockProxy = mock(TestAgentWithOutputKey.class);
        A2AClientBuilder<TestAgentWithOutputKey> a2aBuilder = stubA2ABuilder(mockProxy);

        try (MockedStatic<AgenticServices> agSvc = mockStatic(AgenticServices.class)) {
            agSvc.when(() -> AgenticServices.a2aBuilder("http://agent.example.com", TestAgentWithOutputKey.class))
                    .thenReturn(a2aBuilder);

            subject.build(TestAgentWithOutputKey.class, "http://agent.example.com", "answer", false, "", lookup);

            verify(a2aBuilder).outputKey("answer");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void build_withAsyncTrue_setsAsyncFlagOnBuilder() {
        DefaultA2AAgentBuilder subject = new DefaultA2AAgentBuilder();
        Instance<Object> lookup = mock(Instance.class);
        TestAsyncAgent mockProxy = mock(TestAsyncAgent.class);
        A2AClientBuilder<TestAsyncAgent> a2aBuilder = stubA2ABuilder(mockProxy);

        try (MockedStatic<AgenticServices> agSvc = mockStatic(AgenticServices.class)) {
            agSvc.when(() -> AgenticServices.a2aBuilder("http://agent.example.com", TestAsyncAgent.class))
                    .thenReturn(a2aBuilder);

            subject.build(TestAsyncAgent.class, "http://agent.example.com", "", true, "", lookup);

            verify(a2aBuilder).async(true);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void build_withAgentListenerInCdi_setsListenerOnBuilder() {
        DefaultA2AAgentBuilder subject = new DefaultA2AAgentBuilder();
        Instance<Object> lookup = mock(Instance.class);
        Instance<AgentListener> listenerInstance = mock(Instance.class);
        AgentListener listener = mock(AgentListener.class);
        when(lookup.select(AgentListener.class, NamedLiteral.of("myListener"))).thenReturn(listenerInstance);
        when(listenerInstance.isResolvable()).thenReturn(true);
        when(listenerInstance.get()).thenReturn(listener);
        TestAgentWithListener mockProxy = mock(TestAgentWithListener.class);
        A2AClientBuilder<TestAgentWithListener> a2aBuilder = stubA2ABuilder(mockProxy);

        try (MockedStatic<AgenticServices> agSvc = mockStatic(AgenticServices.class)) {
            agSvc.when(() -> AgenticServices.a2aBuilder("http://agent.example.com", TestAgentWithListener.class))
                    .thenReturn(a2aBuilder);

            subject.build(TestAgentWithListener.class, "http://agent.example.com", "", false, "myListener", lookup);

            verify(a2aBuilder).listener(listener);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void build_whenNoAgentListenerInCdi_doesNotSetListener() {
        DefaultA2AAgentBuilder subject = new DefaultA2AAgentBuilder();
        Instance<Object> lookup = mock(Instance.class);
        TestAgent mockProxy = mock(TestAgent.class);
        A2AClientBuilder<TestAgent> a2aBuilder = stubA2ABuilder(mockProxy);

        try (MockedStatic<AgenticServices> agSvc = mockStatic(AgenticServices.class)) {
            agSvc.when(() -> AgenticServices.a2aBuilder("http://agent.example.com", TestAgent.class))
                    .thenReturn(a2aBuilder);

            subject.build(TestAgent.class, "http://agent.example.com", "", false, "", lookup);

            verify(a2aBuilder, never()).listener(any());
        }
    }
}

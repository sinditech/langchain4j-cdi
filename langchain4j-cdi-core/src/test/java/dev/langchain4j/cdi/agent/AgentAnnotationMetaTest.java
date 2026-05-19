package dev.langchain4j.cdi.agent;

import static org.junit.jupiter.api.Assertions.*;

import dev.langchain4j.cdi.spi.RegisterA2AAgent;
import dev.langchain4j.cdi.spi.RegisterConditionalAgent;
import dev.langchain4j.cdi.spi.RegisterHumanInTheLoopAgent;
import dev.langchain4j.cdi.spi.RegisterLoopAgent;
import dev.langchain4j.cdi.spi.RegisterMcpClientAgent;
import dev.langchain4j.cdi.spi.RegisterParallelAgent;
import dev.langchain4j.cdi.spi.RegisterParallelMapperAgent;
import dev.langchain4j.cdi.spi.RegisterPlannerAgent;
import dev.langchain4j.cdi.spi.RegisterSequenceAgent;
import dev.langchain4j.cdi.spi.RegisterSimpleAgent;
import dev.langchain4j.cdi.spi.RegisterSupervisorAgent;
import jakarta.enterprise.context.ApplicationScoped;
import org.junit.jupiter.api.Test;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
class AgentAnnotationMetaTest {

    // --- One annotated interface per topology ---

    @RegisterSimpleAgent(name = "simple", description = "simple desc", outputKey = "simpleOut", async = true)
    interface SimpleInterface {}

    @RegisterSequenceAgent(name = "seq", description = "seq desc", outputKey = "seqOut")
    interface SequenceInterface {}

    @RegisterLoopAgent(name = "loop", description = "loop desc", outputKey = "loopOut")
    interface LoopInterface {}

    @RegisterParallelAgent(name = "par", description = "par desc", outputKey = "parOut")
    interface ParallelInterface {}

    @RegisterParallelMapperAgent(
            name = "mapper",
            description = "mapper desc",
            outputKey = "mapOut",
            subAgentNames = {"worker"},
            itemsKey = "items")
    interface ParallelMapperInterface {}

    @RegisterConditionalAgent(name = "cond", description = "cond desc", outputKey = "condOut")
    interface ConditionalInterface {}

    @RegisterSupervisorAgent(name = "sup", description = "sup desc", outputKey = "supOut")
    interface SupervisorInterface {}

    @RegisterPlannerAgent(name = "plan", description = "plan desc", outputKey = "planOut")
    interface PlannerInterface {}

    @RegisterA2AAgent(
            name = "a2a",
            description = "a2a desc",
            outputKey = "a2aOut",
            a2aServerUrl = "http://remote-agent:8080",
            async = true)
    interface A2AInterface {}

    @RegisterMcpClientAgent(
            name = "mcp",
            description = "mcp desc",
            outputKey = "mcpOut",
            mcpClientName = "myMcpClient",
            mcpToolName = "web_search")
    interface McpClientInterface {}

    @RegisterHumanInTheLoopAgent(name = "hitl", description = "hitl desc", outputKey = "hitlOut", async = true)
    interface HumanInTheLoopInterface {}

    interface PlainInterface {}

    // =========================================================================
    // detect() — null for unannotated or non-interface
    // =========================================================================

    @Test
    void detect_unannotatedInterface_returnsNull() {
        assertNull(AgentAnnotationMeta.detect(PlainInterface.class));
    }

    @Test
    void detect_concreteClass_returnsNull() {
        assertNull(AgentAnnotationMeta.detect(Object.class));
    }

    // =========================================================================
    // detect() — each topology sets the correct annotationClass
    // =========================================================================

    @Test
    void detect_simpleAgent_returnsCorrectMeta() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(SimpleInterface.class);

        assertNotNull(meta);
        assertEquals(RegisterSimpleAgent.class, meta.annotationClass());
        assertEquals("simple", meta.rawName());
        assertEquals("simple desc", meta.rawDescription());
        assertEquals("simpleOut", meta.rawOutputKey());
        assertTrue(meta.async());
        assertEquals(ApplicationScoped.class, meta.scope());
    }

    @Test
    void detect_sequenceAgent_setsAnnotationClassAndNoAsync() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(SequenceInterface.class);

        assertNotNull(meta);
        assertEquals(RegisterSequenceAgent.class, meta.annotationClass());
        assertEquals("seq", meta.rawName());
        assertFalse(meta.async());
    }

    @Test
    void detect_loopAgent_setsAnnotationClassAndNoAsync() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(LoopInterface.class);

        assertNotNull(meta);
        assertEquals(RegisterLoopAgent.class, meta.annotationClass());
        assertFalse(meta.async());
    }

    @Test
    void detect_parallelAgent_setsAnnotationClassAndNoAsync() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(ParallelInterface.class);

        assertNotNull(meta);
        assertEquals(RegisterParallelAgent.class, meta.annotationClass());
        assertFalse(meta.async());
    }

    @Test
    void detect_parallelMapperAgent_setsAnnotationClassAndNoAsync() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(ParallelMapperInterface.class);

        assertNotNull(meta);
        assertEquals(RegisterParallelMapperAgent.class, meta.annotationClass());
        assertEquals("mapper", meta.rawName());
        assertEquals("mapOut", meta.rawOutputKey());
        assertFalse(meta.async());
    }

    @Test
    void detect_conditionalAgent_setsAnnotationClassAndNoAsync() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(ConditionalInterface.class);

        assertNotNull(meta);
        assertEquals(RegisterConditionalAgent.class, meta.annotationClass());
        assertFalse(meta.async());
    }

    @Test
    void detect_supervisorAgent_setsAnnotationClassAndNoAsync() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(SupervisorInterface.class);

        assertNotNull(meta);
        assertEquals(RegisterSupervisorAgent.class, meta.annotationClass());
        assertFalse(meta.async());
    }

    @Test
    void detect_plannerAgent_setsAnnotationClassAndNoAsync() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(PlannerInterface.class);

        assertNotNull(meta);
        assertEquals(RegisterPlannerAgent.class, meta.annotationClass());
        assertFalse(meta.async());
    }

    @Test
    void detect_a2aAgent_setsAnnotationClassAndReflectsAsync() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(A2AInterface.class);

        assertNotNull(meta);
        assertEquals(RegisterA2AAgent.class, meta.annotationClass());
        assertEquals("a2a", meta.rawName());
        assertEquals("a2aOut", meta.rawOutputKey());
        assertTrue(meta.async());
    }

    @Test
    void detect_mcpClientAgent_setsAnnotationClassAndOutputKey() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(McpClientInterface.class);

        assertNotNull(meta);
        assertEquals(RegisterMcpClientAgent.class, meta.annotationClass());
        assertEquals("mcp", meta.rawName());
        // outputKey is supported on MCP_CLIENT despite old README claim to the contrary
        assertEquals("mcpOut", meta.rawOutputKey());
    }

    @Test
    void detect_humanInTheLoopAgent_setsAnnotationClassAndReflectsAsync() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(HumanInTheLoopInterface.class);

        assertNotNull(meta);
        assertEquals(RegisterHumanInTheLoopAgent.class, meta.annotationClass());
        assertEquals("hitl", meta.rawName());
        assertTrue(meta.async());
    }

    // =========================================================================
    // name(), description() and outputKey() — resolved via CdiLookupHelper
    // (no EL/Config active in unit tests, so raw values pass through unchanged)
    // =========================================================================

    @Test
    void name_withoutExpressionResolver_returnsRawValue() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(SequenceInterface.class);

        assertNotNull(meta);
        assertEquals(meta.rawName(), meta.name());
    }

    @Test
    void description_withoutExpressionResolver_returnsRawValue() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(SequenceInterface.class);

        assertNotNull(meta);
        assertEquals(meta.rawDescription(), meta.description());
    }

    @Test
    void outputKey_withoutExpressionResolver_returnsRawValue() {
        AgentAnnotationMeta meta = AgentAnnotationMeta.detect(SequenceInterface.class);

        assertNotNull(meta);
        assertEquals(meta.rawOutputKey(), meta.outputKey());
    }

    // =========================================================================
    // isAgentInterface()
    // =========================================================================

    @Test
    void isAgentInterface_annotatedInterface_returnsTrue() {
        assertAll(
                () -> assertTrue(AgentAnnotationMeta.isAgentInterface(SimpleInterface.class)),
                () -> assertTrue(AgentAnnotationMeta.isAgentInterface(SequenceInterface.class)),
                () -> assertTrue(AgentAnnotationMeta.isAgentInterface(LoopInterface.class)),
                () -> assertTrue(AgentAnnotationMeta.isAgentInterface(ParallelInterface.class)),
                () -> assertTrue(AgentAnnotationMeta.isAgentInterface(ParallelMapperInterface.class)),
                () -> assertTrue(AgentAnnotationMeta.isAgentInterface(ConditionalInterface.class)),
                () -> assertTrue(AgentAnnotationMeta.isAgentInterface(SupervisorInterface.class)),
                () -> assertTrue(AgentAnnotationMeta.isAgentInterface(PlannerInterface.class)),
                () -> assertTrue(AgentAnnotationMeta.isAgentInterface(A2AInterface.class)),
                () -> assertTrue(AgentAnnotationMeta.isAgentInterface(McpClientInterface.class)),
                () -> assertTrue(AgentAnnotationMeta.isAgentInterface(HumanInTheLoopInterface.class)));
    }

    @Test
    void isAgentInterface_unannotatedInterface_returnsFalse() {
        assertFalse(AgentAnnotationMeta.isAgentInterface(PlainInterface.class));
    }

    @Test
    void isAgentInterface_annotatedClass_returnsFalse() {
        assertFalse(AgentAnnotationMeta.isAgentInterface(Object.class));
    }
}

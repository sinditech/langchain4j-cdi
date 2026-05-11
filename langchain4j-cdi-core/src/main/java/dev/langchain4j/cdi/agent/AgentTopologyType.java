package dev.langchain4j.cdi.agent;

/** Topology of an agentic system registered via {@link dev.langchain4j.cdi.spi.RegisterAgent}. */
public enum AgentTopologyType {
    SIMPLE,
    SEQUENCE,
    LOOP,
    PARALLEL,
    CONDITIONAL,
    SUPERVISOR,
    PLANNER,
    A2A
}

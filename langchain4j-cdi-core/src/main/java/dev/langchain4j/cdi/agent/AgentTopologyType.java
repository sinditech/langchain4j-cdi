package dev.langchain4j.cdi.agent;

/** Topology of an agentic system. */
public enum AgentTopologyType {
    SIMPLE,
    SEQUENCE,
    LOOP,
    PARALLEL,
    /** Maps a list of items over a single sub-agent, executing each mapping in parallel. */
    PARALLEL_MAPPER,
    CONDITIONAL,
    SUPERVISOR,
    PLANNER,
    A2A,
    /** Wraps an MCP server tool as an agent. */
    MCP_CLIENT,
    /** Injects a human-in-the-loop step that pauses execution and waits for human input. */
    HUMAN_IN_THE_LOOP
}

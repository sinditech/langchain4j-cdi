/** */
package dev.langchain4j.cdi.telemetry;

/**
 * Creates metrics that follow the <a
 * href="https://opentelemetry.io/docs/specs/semconv/gen-ai/gen-ai-agent-spans/">Semantic Conventions for GenAI agent
 * and framework spans</a>.
 *
 * @author Buhake Sindi
 * @since 21 June 2026
 */
public enum GenAIOperations {
    CHAT("chat", "Chat completion operation such as OpenAI Chat API"),
    CREATE_AGENT("create_agent", "Create GenAI agent"),
    CREATE_MEMORY("create_memory", "Create new memory records"),
    CREATE_MEMORY_STORE("create_memory_store", "Create or initialize a memory store"),
    DELETE_MEMORY("delete_memory", "Delete memory records"),
    DELETE_MEMORY_STORE("delete_memory_store", "Delete or deprovision a memory store"),
    EMBEDDINGS("embeddings", "Embeddings operation such as OpenAI Create embeddings API"),
    EXECUTE_TOOL("execute_tool", "Execute a tool"),
    GENERATE_CONTENT("generate_content", "Multimodal content generation operation such as Gemini Generate Content"),
    INVOKE_AGENT("invoke_agent", "Invoke GenAI agent"),
    INVOKE_WORKFLOW("invoke_workflow", "Invoke GenAI workflow"),
    PLAN("plan", "Agent planning or task decomposition phase"),
    RETRIEVAL("retrieval", "Retrieval operation such as OpenAI Search Vector Store API"),
    SEARCH_MEMORY("search_memory", "Search/query memories from a memory store"),
    TEXT_COMPLETION("text_completion", "Text completions operation such as OpenAI Completions API (Legacy)"),
    UPDATE_MEMORY("update_memory", "Update existing memory records"),
    UPSERT_MEMORY("upsert_memory", "Create or update memory records without the caller choosing which");

    private final String operationName;
    private final String description;

    /**
     * @param operationName
     * @param description
     */
    private GenAIOperations(final String operationName, final String description) {
        this.operationName = operationName;
        this.description = description;
    }

    /** @return the description */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return operationName;
    }
}

/** */
package dev.langchain4j.cdi.telemetry.conventions;

/**
 * Enum representing the GenAI semantic convention attributes. Each constant holds the exact attribute key as defined in
 * the OpenTelemetry Semantic Conventions for GenAI.
 *
 * @author Buhake Sindi
 * @since 09 October 2026
 * @see <a
 *     href="https://github.com/open-telemetry/semantic-conventions-genai/blob/main/docs/registry/attributes/gen-ai.md">Gen
 *     AI attributes</a>.
 */
public enum GenAIAttributes {

    // Agent attributes
    AGENT_DESCRIPTION("gen_ai.agent.description"),
    AGENT_ID("gen_ai.agent.id"),
    AGENT_NAME("gen_ai.agent.name"),
    AGENT_VERSION("gen_ai.agent.version"),

    // Conversation attributes
    CONVERSATION_COMPACTED("gen_ai.conversation.compacted"),
    CONVERSATION_ID("gen_ai.conversation.id"),

    // Data source attributes
    DATA_SOURCE_ID("gen_ai.data_source.id"),

    // Embeddings attributes
    EMBEDDINGS_DIMENSION_COUNT("gen_ai.embeddings.dimension.count"),

    // Evaluation attributes
    EVALUATION_EXPLANATION("gen_ai.evaluation.explanation"),
    EVALUATION_NAME("gen_ai.evaluation.name"),
    EVALUATION_SCORE_LABEL("gen_ai.evaluation.score.label"),
    EVALUATION_SCORE_VALUE("gen_ai.evaluation.score.value"),

    // Input attributes
    INPUT_MESSAGES("gen_ai.input.messages"),

    // Main agent attributes
    MAIN_AGENT_DESCRIPTION("gen_ai.main_agent.description"),
    MAIN_AGENT_ID("gen_ai.main_agent.id"),
    MAIN_AGENT_NAME("gen_ai.main_agent.name"),

    // Memory attributes
    MEMORY_QUERY_TEXT("gen_ai.memory.query.text"),
    MEMORY_RECORD_COUNT("gen_ai.memory.record.count"),
    MEMORY_RECORD_ID("gen_ai.memory.record.id"),
    MEMORY_RECORDS("gen_ai.memory.records"),
    MEMORY_STORE_ID("gen_ai.memory.store.id"),

    // Operation attributes
    OPERATION_NAME("gen_ai.operation.name"),

    // Output attributes
    OUTPUT_MESSAGES("gen_ai.output.messages"),
    OUTPUT_TYPE("gen_ai.output.type"),

    // Prompt attributes
    PROMPT_NAME("gen_ai.prompt.name"),
    PROMPT_VARIABLE("gen_ai.prompt.variable"),
    PROMPT_VERSION("gen_ai.prompt.version"),

    // Provider attributes
    PROVIDER_NAME("gen_ai.provider.name"),

    // Request attributes
    REQUEST_CHOICE_COUNT("gen_ai.request.choice.count"),
    REQUEST_ENCODING_FORMATS("gen_ai.request.encoding_formats"),
    REQUEST_FREQUENCY_PENALTY("gen_ai.request.frequency_penalty"),
    REQUEST_MAX_TOKENS("gen_ai.request.max_tokens"),
    REQUEST_MODEL("gen_ai.request.model"),
    REQUEST_PRESENCE_PENALTY("gen_ai.request.presence_penalty"),
    REQUEST_PREVIOUS_RESPONSE_ID("gen_ai.request.previous_response.id"),
    REQUEST_REASONING_LEVEL("gen_ai.request.reasoning.level"),
    REQUEST_SEED("gen_ai.request.seed"),
    REQUEST_STOP_SEQUENCES("gen_ai.request.stop_sequences"),
    REQUEST_STREAM("gen_ai.request.stream"),
    REQUEST_STREAM_CURSOR("gen_ai.request.stream_cursor"),
    REQUEST_TEMPERATURE("gen_ai.request.temperature"),
    REQUEST_TOP_K("gen_ai.request.top_k"),
    REQUEST_TOP_P("gen_ai.request.top_p"),

    // Response attributes
    RESPONSE_FINISH_REASONS("gen_ai.response.finish_reasons"),
    RESPONSE_ID("gen_ai.response.id"),
    RESPONSE_MODEL("gen_ai.response.model"),
    RESPONSE_STATUS("gen_ai.response.status"),
    RESPONSE_TIME_TO_FIRST_CHUNK("gen_ai.response.time_to_first_chunk"),

    // Retrieval attributes
    RETRIEVAL_DOCUMENTS("gen_ai.retrieval.documents"),
    RETRIEVAL_QUERY_TEXT("gen_ai.retrieval.query.text"),
    RETRIEVAL_TOP_K("gen_ai.retrieval.top_k"),

    // Skill attributes
    SKILL_DESCRIPTION("gen_ai.skill.description"),
    SKILL_NAME("gen_ai.skill.name"),
    SKILL_RESOURCE_NAME("gen_ai.skill.resource.name"),
    SKILL_SOURCE_URI("gen_ai.skill.source.uri"),

    // System instructions
    SYSTEM_INSTRUCTIONS("gen_ai.system_instructions"),

    // Token modality
    TOKEN_MODALITY("gen_ai.token.modality"),

    // Tool attributes
    TOOL_CALL_ARGUMENTS("gen_ai.tool.call.arguments"),
    TOOL_CALL_ID("gen_ai.tool.call.id"),
    TOOL_CALL_RESULT("gen_ai.tool.call.result"),
    TOOL_DEFINITIONS("gen_ai.tool.definitions"),
    TOOL_DESCRIPTION("gen_ai.tool.description"),
    TOOL_NAME("gen_ai.tool.name"),
    TOOL_TYPE("gen_ai.tool.type"),

    // Usage – audio
    USAGE_AUDIO_CACHE_READ_INPUT_TOKENS("gen_ai.usage.audio.cache_read.input_tokens"),
    USAGE_AUDIO_INPUT_TOKENS("gen_ai.usage.audio.input_tokens"),
    USAGE_AUDIO_OUTPUT_TOKENS("gen_ai.usage.audio.output_tokens"),

    // Usage – cache
    USAGE_CACHE_READ_INPUT_TOKENS("gen_ai.usage.cache_read.input_tokens"),
    USAGE_CACHE_WRITE_INPUT_TOKENS("gen_ai.usage.cache_write.input_tokens"),

    // Usage – image
    USAGE_IMAGE_CACHE_READ_INPUT_TOKENS("gen_ai.usage.image.cache_read.input_tokens"),
    USAGE_IMAGE_INPUT_TOKENS("gen_ai.usage.image.input_tokens"),
    USAGE_IMAGE_OUTPUT_TOKENS("gen_ai.usage.image.output_tokens"),

    // Usage – general input/output
    USAGE_INPUT_TOKENS("gen_ai.usage.input_tokens"),
    USAGE_OUTPUT_TOKENS("gen_ai.usage.output_tokens"),
    USAGE_REASONING_OUTPUT_TOKENS("gen_ai.usage.reasoning.output_tokens"),

    // Usage – text
    USAGE_TEXT_CACHE_READ_INPUT_TOKENS("gen_ai.usage.text.cache_read.input_tokens"),
    USAGE_TEXT_INPUT_TOKENS("gen_ai.usage.text.input_tokens"),
    USAGE_TEXT_OUTPUT_TOKENS("gen_ai.usage.text.output_tokens"),

    // Workflow attributes
    WORKFLOW_NAME("gen_ai.workflow.name"),

    // Error

    /** The type of error that occurred. */
    ERROR_TYPE("error.type"),

    // GenAi server information
    /** The GenAI server port. */
    SERVER_PORT("server.port"),

    /** The GenAI server address. */
    SERVER_ADDRESS("server.address");

    private final String value;

    /** @param value */
    private GenAIAttributes(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

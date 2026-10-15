/** */
package dev.langchain4j.cdi.telemetry.conventions;

/**
 * Enum representing the GenAI metric instruments and their attribute keys as defined in the OpenTelemetry Semantic
 * Conventions for Generative AI metrics.
 *
 * @author Buhake Sindi
 * @since 09 October 2026
 * @see <a
 *     href="https://github.com/open-telemetry/semantic-conventions-genai/blob/main/docs/gen-ai/gen-ai-metrics.md">GenAI
 *     Metrics</a>.
 */
public enum GenAIMetrics {
    CLIENT_OPERATION_DURATION("gen_ai.client.operation.duration"),
    SERVER_REQUEST_DURATION("gen_ai.server.request.duration"),
    SERVER_TIME_PER_OUTPUT_TOKEN("gen_ai.server.time_per_output_token"),
    SERVER_TIME_TO_FIRST_TOKEN("gen_ai.server.time_to_first_token"),

    INVOKE_WORKFLOW_DURATION("gen_ai.invoke_workflow.duration");

    private final String value;

    /** @param value */
    private GenAIMetrics(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

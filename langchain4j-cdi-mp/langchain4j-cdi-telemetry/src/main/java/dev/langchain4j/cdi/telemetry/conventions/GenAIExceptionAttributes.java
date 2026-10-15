/** */
package dev.langchain4j.cdi.telemetry.conventions;

/**
 * Enum representing the exception attributes and event name for Generative AI client operations, as defined in the
 * OpenTelemetry Semantic Conventions for GenAI exceptions.
 *
 * @author Buhake Sindi
 * @since 12 October 2026
 * @see <a
 *     href="https://github.com/open-telemetry/semantic-conventions-genai/blob/main/docs/gen-ai/gen-ai-exceptions.md">GenAI
 *     Exceptions</a>
 */
public enum GenAIExceptionAttributes {

    // Exception event name
    CLIENT_OPERATION_EXCEPTION_EVENT("gen_ai.client.operation.exception"),

    // Exception attributes
    EXCEPTION_MESSAGE("exception.message"),
    EXCEPTION_TYPE("exception.type"),
    EXCEPTION_STACKTRACE("exception.stacktrace");

    private final String value;

    /** @param value */
    private GenAIExceptionAttributes(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}

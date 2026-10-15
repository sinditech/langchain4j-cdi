package dev.langchain4j.cdi.telemetry;

import dev.langchain4j.cdi.telemetry.conventions.GenAIAttributes;
import dev.langchain4j.cdi.telemetry.conventions.GenAIOperations;
import dev.langchain4j.cdi.telemetry.conventions.GenAIProviders;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import jakarta.enterprise.context.Dependent;
import java.util.Map;

/**
 * Creates metrics that follow the <a href="https://opentelemetry.io/docs/specs/semconv/gen-ai/gen-ai-metrics/">Semantic
 * Conventions for GenAI Metrics</a>.
 *
 * @author Buhake Sindi
 * @since 25 November 2024
 */
@Dependent
public class GenAIMetricsChatModelListener extends GenAIMetricsTelemetry implements ChatModelListener {

    /** Creates a new instance. */
    public GenAIMetricsChatModelListener() {}

    private static final String MP_AI_METRIC_START_TIME_NAME = "MP_AI_CHAT_METRIC_START_TIME";

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        requestContext.attributes().put(MP_AI_METRIC_START_TIME_NAME, System.nanoTime());
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        recordClientOperationDurationMetrics(responseContext);
        recordClientTokenUsageMetrics(responseContext);
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        recordErrorDurationMetrics(errorContext);
    }

    // ---------------------------------- Utility methods ----------------------------------

    private void recordClientTokenUsageMetrics(final ChatModelResponseContext responseContext) {
        final TokenUsage tokenUsage = responseContext.chatResponse().tokenUsage();
        if (tokenUsage == null) return;

        if (tokenUsage.inputTokenCount() != null) {
            Attributes inputTokenCountAttributes = Attributes.of(
                    AttributeKey.stringKey(GenAIAttributes.OPERATION_NAME.toString()),
                    GenAIOperations.CHAT.toString(),
                    AttributeKey.stringKey(GenAIAttributes.PROVIDER_NAME.toString()),
                    GenAIProviders.of(responseContext.modelProvider()).toString(),
                    AttributeKey.stringKey(GenAIAttributes.REQUEST_MODEL.toString()),
                    responseContext.chatRequest().modelName(),
                    AttributeKey.stringKey(GenAIAttributes.RESPONSE_MODEL.toString()),
                    responseContext.chatResponse().modelName(),
                    TOKEN_TYPE,
                    "input");

            // Record
            recordClientTokenUsageMetrics(tokenUsage.inputTokenCount(), inputTokenCountAttributes);
        }

        if (tokenUsage.outputTokenCount() != null) {
            Attributes outputTokenCountAttributes = Attributes.of(
                    AttributeKey.stringKey(GenAIAttributes.OPERATION_NAME.toString()),
                    GenAIOperations.CHAT.toString(),
                    AttributeKey.stringKey(GenAIAttributes.PROVIDER_NAME.toString()),
                    GenAIProviders.of(responseContext.modelProvider()).toString(),
                    AttributeKey.stringKey(GenAIAttributes.REQUEST_MODEL.toString()),
                    responseContext.chatRequest().modelName(),
                    AttributeKey.stringKey(GenAIAttributes.RESPONSE_MODEL.toString()),
                    responseContext.chatResponse().modelName(),
                    TOKEN_TYPE,
                    "output");

            // Record
            recordClientTokenUsageMetrics(tokenUsage.outputTokenCount(), outputTokenCountAttributes);
        }
    }

    private void recordClientOperationDurationMetrics(final ChatModelResponseContext responseContext) {
        Long startTime = retrieveStartTime(responseContext.attributes());
        if (startTime == null) return;
        long endTime = System.nanoTime();

        // Record duration
        Attributes durationAttributes = Attributes.of(
                AttributeKey.stringKey(GenAIAttributes.OPERATION_NAME.toString()),
                GenAIOperations.CHAT.toString(),
                AttributeKey.stringKey(GenAIAttributes.PROVIDER_NAME.toString()),
                GenAIProviders.of(responseContext.modelProvider()).toString(),
                AttributeKey.stringKey(GenAIAttributes.REQUEST_MODEL.toString()),
                responseContext.chatRequest().modelName(),
                AttributeKey.stringKey(GenAIAttributes.RESPONSE_MODEL.toString()),
                responseContext.chatResponse().modelName());

        recordClientOperationDurationMetrics(startTime, endTime, durationAttributes);
    }

    private void recordErrorDurationMetrics(final ChatModelErrorContext errorContext) {
        Long startTime = retrieveStartTime(errorContext.attributes());
        if (startTime == null) return;
        long endTime = System.nanoTime();

        Throwable error = errorContext.error();
        StringBuilder sb = new StringBuilder()
                .append(error.getClass().getName())
                .append(";")
                .append(error.getLocalizedMessage());

        // Record duration
        Attributes durationAttributes = Attributes.of(
                AttributeKey.stringKey(GenAIAttributes.OPERATION_NAME.toString()),
                GenAIOperations.CHAT.toString(),
                AttributeKey.stringKey(GenAIAttributes.PROVIDER_NAME.toString()),
                GenAIProviders.of(errorContext.modelProvider()).toString(),
                AttributeKey.stringKey(GenAIAttributes.REQUEST_MODEL.toString()),
                errorContext.chatRequest().modelName(),
                AttributeKey.stringKey(GenAIAttributes.ERROR_TYPE.toString()),
                sb.toString());

        recordClientOperationDurationMetrics(startTime, endTime, durationAttributes);
    }

    private static Long retrieveStartTime(final Map<Object, Object> attributes) {
        return (Long) attributes.remove(MP_AI_METRIC_START_TIME_NAME);
    }
}

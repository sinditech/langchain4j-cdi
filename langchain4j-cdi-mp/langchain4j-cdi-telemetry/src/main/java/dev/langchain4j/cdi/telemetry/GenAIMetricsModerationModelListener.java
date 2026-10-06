package dev.langchain4j.cdi.telemetry;

import dev.langchain4j.cdi.telemetry.conventions.GenAIAttributes;
import dev.langchain4j.cdi.telemetry.conventions.GenAIProviders;
import dev.langchain4j.model.moderation.listener.ModerationModelErrorContext;
import dev.langchain4j.model.moderation.listener.ModerationModelListener;
import dev.langchain4j.model.moderation.listener.ModerationModelRequestContext;
import dev.langchain4j.model.moderation.listener.ModerationModelResponseContext;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import jakarta.enterprise.context.Dependent;
import java.util.Map;

/**
 * Creates metrics that follow the <a href="https://opentelemetry.io/docs/specs/semconv/gen-ai/gen-ai-metrics/">Semantic
 * Conventions for GenAI Metrics</a>.
 *
 * @author Buhake Sindi
 * @since 12 October 2026
 */
@Dependent
public class GenAIMetricsModerationModelListener extends GenAIMetricsTelemetry implements ModerationModelListener {

    /** Creates a new instance. */
    public GenAIMetricsModerationModelListener() {}

    private static final String MP_AI_METRIC_START_TIME_NAME = "MP_AI_MODERATION_METRIC_START_TIME";

    @Override
    public void onRequest(ModerationModelRequestContext requestContext) {
        requestContext.attributes().put(MP_AI_METRIC_START_TIME_NAME, System.nanoTime());
    }

    @Override
    public void onResponse(ModerationModelResponseContext responseContext) {
        recordClientOperationDurationMetrics(responseContext);
    }

    @Override
    public void onError(ModerationModelErrorContext errorContext) {
        recordErrorDurationMetrics(errorContext);
    }

    // ---------------------------------- Utility methods ----------------------------------

    private void recordClientOperationDurationMetrics(final ModerationModelResponseContext responseContext) {
        Long startTime = retrieveStartTime(responseContext.attributes());
        if (startTime == null) return;
        long endTime = System.nanoTime();

        // Record duration
        Attributes durationAttributes = Attributes.of(
                AttributeKey.stringKey(GenAIAttributes.OPERATION_NAME.toString()),
                "moderation",
                AttributeKey.stringKey(GenAIAttributes.PROVIDER_NAME.toString()),
                GenAIProviders.of(responseContext.modelProvider()).toString(),
                AttributeKey.stringKey(GenAIAttributes.REQUEST_MODEL.toString()),
                responseContext.moderationRequest().modelName());

        recordClientOperationDurationMetrics(startTime, endTime, durationAttributes);
    }

    private void recordErrorDurationMetrics(final ModerationModelErrorContext errorContext) {
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
                "moderation",
                AttributeKey.stringKey(GenAIAttributes.PROVIDER_NAME.toString()),
                GenAIProviders.of(errorContext.modelProvider()).toString(),
                AttributeKey.stringKey(GenAIAttributes.REQUEST_MODEL.toString()),
                errorContext.moderationRequest().modelName(),
                AttributeKey.stringKey(GenAIAttributes.ERROR_TYPE.toString()),
                sb.toString());

        recordClientOperationDurationMetrics(startTime, endTime, durationAttributes);
    }

    private static Long retrieveStartTime(final Map<Object, Object> attributes) {
        return (Long) attributes.remove(MP_AI_METRIC_START_TIME_NAME);
    }
}

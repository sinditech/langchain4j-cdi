/** */
package dev.langchain4j.cdi.telemetry.conventions;

import dev.langchain4j.model.ModelProvider;

/**
 * Maps {@link ModelProvider} values to OpenTelemetry Semantic Conventions (v1.39.0) well-known values for the
 * {@code gen_ai.provider.name} attribute.
 *
 * @see <a href="https://opentelemetry.io/docs/specs/semconv/gen-ai/gen-ai-spans/">OTel GenAI Spans</a>
 * @author Buhake Sindi
 * @since 09 October 2026
 */
public enum GenAIProviders {
    ANTHROPIC(ModelProvider.ANTHROPIC, "anthropic"),
    AMAZON_BEDROCK(ModelProvider.AMAZON_BEDROCK, "aws.bedrock"),
    // Azure OpenAI is deprecated in favor of Microsoft Foundry
    @Deprecated
    AZURE_OPEN_AI(ModelProvider.AZURE_OPEN_AI, "azure.ai.openai"),
    COHERE(ModelProvider.COHERE, "cohere"),
    GITHUB_MODELS(ModelProvider.GITHUB_MODELS, "github"),
    GOOGLE_AI_GEMINI(ModelProvider.GOOGLE_AI_GEMINI, "gcp.gemini"),
    GOOGLE_GENAI(ModelProvider.GOOGLE_GENAI, "gcp.gen_ai"),
    GOOGLE_VERTEX_AI_GEMINI(ModelProvider.GOOGLE_VERTEX_AI_GEMINI, "gcp.vertex_ai"),
    GOOGLE_VERTEX_AI_ANTHROPIC(ModelProvider.GOOGLE_VERTEX_AI_ANTHROPIC, "gcp.vertex_ai"),
    JINA(ModelProvider.JINA, "jina"),
    MICROSOFT_FOUNDRY(ModelProvider.MICROSOFT_FOUNDRY, "azure.ai.inference"),
    MISTRAL_AI(ModelProvider.MISTRAL_AI, "mistral_ai"),
    OLLAMA(ModelProvider.OLLAMA, "ollama"),
    OPEN_AI(ModelProvider.OPEN_AI, "openai"),
    TYPESAFE(ModelProvider.TYPESAFE, "typesafe"),
    VOYAGE_AI(ModelProvider.VOYAGE_AI, "voyage_ai"),
    WATSONX(ModelProvider.WATSONX, "ibm.watsonx.ai"),
    OTHER(ModelProvider.OTHER, "unknown");

    private final ModelProvider modelProvider;
    private final String value;

    /**
     * @param modelProvider
     * @param value
     */
    private GenAIProviders(ModelProvider modelProvider, String value) {
        this.modelProvider = modelProvider;
        this.value = value;
    }

    /** @return the modelProvider */
    public ModelProvider getModelProvider() {
        return modelProvider;
    }

    public static GenAIProviders of(final ModelProvider modelProvider) {
        for (GenAIProviders providers : values()) {
            if (providers.modelProvider == modelProvider) return providers;
        }

        return OTHER;
    }

    @Override
    public String toString() {
        return value;
    }
}

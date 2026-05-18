package dev.langchain4j.cdi.spi;

/**
 * SPI for resolving expression strings embedded in agent and AI-service stereotype annotation attributes.
 * Implementations are discovered via {@link java.util.ServiceLoader}.
 *
 * <p>Implementations should resolve expressions embedded in string values — for example, MicroProfile Config
 * expressions like {@code ${my.property}} or system-property references. Strings that contain no expression must be
 * returned unchanged.
 *
 * <p>If multiple implementations are registered, they are applied in discovery order: the output of each resolver is
 * passed as the input to the next.
 */
public interface ExpressionResolver {

    /**
     * Resolves any expression embedded in {@code value}.
     *
     * @param value the raw annotation attribute value, never {@code null}
     * @return the resolved value, never {@code null}
     */
    String resolve(String value);
}

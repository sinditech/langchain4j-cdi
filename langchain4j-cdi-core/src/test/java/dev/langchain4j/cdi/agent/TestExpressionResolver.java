package dev.langchain4j.cdi.agent;

import dev.langchain4j.cdi.spi.ExpressionResolver;

/**
 * Test-only ExpressionResolver that strips {@code ${...}} delimiters, so {@code ${#default}} resolves to
 * {@code #default}, {@code ${mem1}} to {@code mem1}, etc. Plain strings are returned unchanged.
 */
public class TestExpressionResolver implements ExpressionResolver {

    @Override
    public String resolve(String value) {
        if (value.startsWith("${") && value.endsWith("}")) {
            return value.substring(2, value.length() - 1);
        }
        return value;
    }
}

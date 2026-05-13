package dev.langchain4j.cdi.core.mpconfig;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MpConfigExpressionResolverTest {

    private final MpConfigExpressionResolver resolver = new MpConfigExpressionResolver();

    @Test
    void resolve_plainString_returnsUnchanged() {
        assertEquals("plainValue", resolver.resolve("plainValue"));
        assertEquals("#default", resolver.resolve("#default"));
    }

    @Test
    void resolve_eelPattern_returnsUnchanged() {
        // #{...} is for EL, not MP Config — must pass through
        assertEquals("#{someBean.prop}", resolver.resolve("#{someBean.prop}"));
    }

    @Test
    void resolve_knownProperty_returnsConfigValue() {
        // "test.agent.model" = "my-model" is defined in src/test/resources/META-INF/microprofile-config.properties
        assertEquals("my-model", resolver.resolve("${test.agent.model}"));
    }

    @Test
    void resolve_knownPropertyForName_returnsConfigValue() {
        assertEquals("reviewAgent", resolver.resolve("${test.agent.name}"));
    }

    @Test
    void resolve_unknownProperty_returnsOriginalExpression() {
        // Unknown key → original expression returned so it is visible
        assertEquals("${nonexistent.key}", resolver.resolve("${nonexistent.key}"));
    }
}

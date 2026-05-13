package dev.langchain4j.cdi.el;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JakartaELExpressionResolverTest {

    private final JakartaELExpressionResolver resolver = new JakartaELExpressionResolver();

    @Test
    void resolve_plainString_returnsUnchanged() {
        assertEquals("plainValue", resolver.resolve("plainValue"));
        assertEquals("#default", resolver.resolve("#default"));
    }

    @Test
    void resolve_mpConfigPattern_returnsUnchanged() {
        // ${...} is for MP Config, not EL — must pass through
        assertEquals("${some.property}", resolver.resolve("${some.property}"));
    }

    @Test
    void resolve_arithmeticExpression_returnsResult() {
        assertEquals("3", resolver.resolve("#{1 + 2}"));
    }

    @Test
    void resolve_stringLiteralExpression_returnsResult() {
        assertEquals("hello", resolver.resolve("#{'hello'}"));
    }

    @Test
    void resolve_conditionalExpression_returnsResult() {
        assertEquals("yes", resolver.resolve("#{true ? 'yes' : 'no'}"));
    }

    @Test
    void resolve_stringConcatenationExpression_returnsResult() {
        // Standalone ELProcessor supports operators and string operations
        assertEquals("foobar", resolver.resolve("#{'foo' += 'bar'}"));
    }

    @Test
    void resolve_invalidExpression_returnsOriginal() {
        // Unparseable expression → original is returned, no exception propagated
        String bad = "#{[[[invalid}";
        assertEquals(bad, resolver.resolve(bad));
    }
}

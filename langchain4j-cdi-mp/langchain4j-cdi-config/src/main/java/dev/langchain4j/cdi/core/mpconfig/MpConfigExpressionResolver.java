package dev.langchain4j.cdi.core.mpconfig;

import dev.langchain4j.cdi.spi.ExpressionResolver;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * ExpressionResolver that resolves {@code ${property.key}} expressions via MicroProfile Config.
 *
 * <p>When a value matches the {@code ${...}} pattern, the enclosed string is used as a MicroProfile Config property
 * key. If the key is found, its value is returned; otherwise the original {@code ${...}} expression is returned
 * unchanged so the problem is visible rather than silently swallowed.
 *
 * <p>Registered automatically via {@link java.util.ServiceLoader} when this module is on the classpath.
 */
public class MpConfigExpressionResolver implements ExpressionResolver {

    private static final Logger LOGGER = Logger.getLogger(MpConfigExpressionResolver.class.getName());

    @Override
    public String resolve(String value) {
        if (!value.startsWith("${") || !value.endsWith("}")) {
            return value;
        }
        String key = value.substring(2, value.length() - 1);
        try {
            return ConfigProvider.getConfig()
                    .getOptionalValue(key, String.class)
                    .orElseGet(() -> {
                        LOGGER.log(
                                Level.FINE,
                                "No MicroProfile Config value found for key ''{0}'', keeping original expression",
                                key);
                        return value;
                    });
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to resolve MicroProfile Config property ''{0}'': {1}", new Object[] {
                key, e.getMessage()
            });
            return value;
        }
    }
}

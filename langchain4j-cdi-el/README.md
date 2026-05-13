# langchain4j-cdi-el — Jakarta EL Expression Resolver

This module provides an `ExpressionResolver` SPI implementation that evaluates **Jakarta Expression Language (EL)** expressions embedded in `#{...}` delimiters within `@RegisterAIService` and `@RegisterAgent` annotation attributes.

## Overview

Annotation attributes like `chatModelName`, `name`, `subAgentNames`, and all `*Name` bean-lookup fields accept `#{...}` expressions evaluated at runtime by this module. This lets you compute bean names dynamically — e.g. based on CDI bean properties, runtime flags, or conditional logic — rather than hardcoding them.

## Dependency

```xml
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-el</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>
```

You also need a Jakarta EL runtime on the classpath. In managed environments (WildFly, Payara, GlassFish, Liberty, Quarkus) one is already provided. In standalone or test scenarios add expressly explicitly:

```xml
<dependency>
    <groupId>org.glassfish.expressly</groupId>
    <artifactId>expressly</artifactId>
    <version>5.0.0</version>
    <scope>runtime</scope>
</dependency>
```

## How It Works

When this module is on the classpath, `JakartaELExpressionResolver` is discovered automatically via `java.util.ServiceLoader` and registered in the `ExpressionResolver` pipeline.

For any annotation attribute value that matches the pattern `#{...}` (the expression must occupy the **entire** value, not just a substring), the enclosed string is evaluated as a Jakarta EL expression using `jakarta.el.ELProcessor`.

**CDI integration:** When CDI is active at resolution time, the `BeanManager`'s `ELResolver` is added to the processor, making `@Named` CDI beans directly accessible by name — e.g. `#{myConfig.modelName}` calls `getModelName()` on the `@Named("myConfig")` bean.

**Non-CDI fallback:** When CDI is not active (e.g. during unit tests), basic EL evaluation still works: arithmetic, string operations, conditionals, etc. Standard implicit EL objects like `systemProperties` are also available.

**Failure handling:** If evaluation fails for any reason the original `#{...}` expression is returned unchanged and a `WARNING` is logged, so the problem is visible rather than silently swallowed.

## Examples

### Conditional Bean Name

Select a model bean based on a runtime flag exposed via a CDI bean:

```java
@RegisterAIService(chatModelName = "#{config.useGpu ? 'gpu-model' : 'cpu-model'}")
public interface SmartAssistant {
    String chat(String message);
}
```

### CDI Bean Property

Delegate the bean name to a `@Named` configuration bean:

```java
@RegisterAgent(name = "#{agentConfig.reviewerName}", chatModelName = "#default")
public interface ReviewAgent {
    @Agent
    String review(@V("text") String text);
}
```

### System Properties

EL's standard `systemProperties` implicit object is available without CDI:

```java
@RegisterAIService(chatModelName = "#{systemProperties['chat.model.bean']}")
public interface EnvironmentAwareService {
    String chat(String message);
}
```

## Combining with MicroProfile Config

Both resolvers can coexist. `${...}` resolves via MicroProfile Config; `#{...}` resolves via Jakarta EL. The delimiters are distinct so there is no conflict. All registered `ExpressionResolver` implementations are applied as a pipeline: the output of each feeds into the next. A resolver that does not recognise the value returns it unchanged.

```java
// Resolved via MicroProfile Config (langchain4j-cdi-config on classpath)
@RegisterAIService(chatModelName = "${my.chat.model.name}")
public interface ConfigDrivenService {
    String chat(String message);
}

// Resolved via Jakarta EL (langchain4j-cdi-el on classpath)
@RegisterAIService(chatModelName = "#{runtimeConfig.chatModelName}")
public interface ElDrivenService {
    String chat(String message);
}
```

## The ExpressionResolver SPI

The `ExpressionResolver` interface (`dev.langchain4j.cdi.spi.ExpressionResolver`) is discovered via `ServiceLoader`. Any number of implementations can coexist; they are all applied to every annotation attribute value in pipeline order.

```java
public interface ExpressionResolver {
    /** Resolves any expression embedded in value. Must return value unchanged if not recognised. */
    String resolve(String value);
}
```

### Custom Resolver Example

```java
public class VaultExpressionResolver implements ExpressionResolver {

    @Override
    public String resolve(String value) {
        if (!value.startsWith("vault:")) {
            return value;
        }
        String secretPath = value.substring(6);
        return VaultClient.readSecret(secretPath);
    }
}
```

Register in `META-INF/services/dev.langchain4j.cdi.spi.ExpressionResolver`:

```
com.example.VaultExpressionResolver
```

### Pipeline Ordering

`ServiceLoader` does not guarantee a fixed discovery order. Since the built-in resolvers use distinct, non-overlapping delimiters (`${...}` for MicroProfile Config, `#{...}` for Jakarta EL), their relative order does not matter — each returns values it does not recognise unchanged. Custom resolvers whose output could match another resolver's pattern should be registered with a well-defined order via the standard `ServiceLoader` ordering mechanisms (e.g. module layers or explicit ordering in `module-info.java`).

## License

Apache License 2.0 — see the [LICENSE](../LICENSE) file.

<p align="center">
  <img src="langchain4j-cdi-logo.png" alt="LangChain4j CDI Logo" width="200"/>
</p>

<p align="center">
  <a href="http://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/github/license/smallrye/smallrye-llm.svg" alt="License"/></a>
  <a href="https://central.sonatype.com/search?q=dev.langchain4j.cdi%3Alangchain4j-cdi-parent"><img src="https://img.shields.io/maven-central/v/dev.langchain4j.cdi/langchain4j-cdi-parent?color=green" alt="Maven"/></a>
  <a href="https://github.com/langchain4j/langchain4j/actions/workflows/main.yaml"><img src="https://img.shields.io/github/actions/workflow/status/langchain4j/langchain4j-cdi/main.yaml?branch=main&style=for-the-badge&label=CI%20BUILD&logo=github" alt="Build Status"/></a>
  <a href="https://discord.gg/JzTFvyjG6R"><img src="https://img.shields.io/discord/1156626270772269217?logoColor=violet" alt="Discord"/></a>
</p>

# LangChain4j CDI Integration

Enterprise CDI extension for LangChain4j - inject AI services directly into your Jakarta EE and MicroProfile applications.

## Table of Contents

- [Overview](#overview)
- [Quick Start](#quick-start)
- [Dependencies](#dependencies)
- [Configuration Reference](#configuration-reference)
- [AI Service Registration](#ai-service-registration)
- [Agent Registration](#agent-registration)
- [Tools and Function Calling](#tools-and-function-calling)
- [RAG (Retrieval Augmented Generation)](#rag-retrieval-augmented-generation)
- [Chat Memory](#chat-memory)
- [MicroProfile Integration](#microprofile-integration)
- [Expression Resolvers](#expression-resolvers)
- [MCP Server](langchain4j-cdi-mcp/README.md) — Expose your CDI beans as an MCP server
- [Examples](#examples)
- [Troubleshooting](#troubleshooting)

---

## Overview

This project provides seamless integration between [LangChain4j](https://docs.langchain4j.dev/) and CDI (Contexts and Dependency Injection), enabling you to:

- **Inject AI services** as CDI beans using `@RegisterAIService`
- **Build agentic systems** with `@RegisterAgent` for multi-agent workflows (sequences, loops, supervisors, A2A)
- **Configure LLM components** via properties (can use microprofile configuration adapter or provide your own)
- **Add resilience** with MicroProfile Fault Tolerance (`@Retry`, `@Timeout`, `@CircuitBreaker`)
- **Monitor AI operations** with MicroProfile Telemetry/OpenTelemetry

### Examples of Supported Runtimes

| Runtime | Extension Type |
|---------|---------------|
| Quarkus | Build-compatible |
| Helidon | Both |
| WildFly | Portable |
| Payara | Portable |
| GlassFish | Portable |
| Liberty | Portable |

---

## Quick Start

### 1. Add Dependencies

**For portable extension (WildFly, Payara, GlassFish, Liberty):**

```xml
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-portable-ext</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>
```

**For build-compatible extension (Quarkus, Helidon):**

```xml
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-build-compatible-ext</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>
```


**For configuration using properties:**

Provide your own `LLMConfig` SPI implementation (see [Configuration Architecture](#configuration-architecture)),

or, if you use MicroProfile:

```xml
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-config</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>
```

**Add your LLM provider dependency:**

```xml
<!-- For Ollama (local models) -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-ollama</artifactId>
    <version>${langchain4j.version}</version>
</dependency>

<!-- For OpenAI -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>${langchain4j.version}</version>
</dependency>

<!-- For Anthropic Claude -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-anthropic</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
```

### 2. Configure the Chat Model 

For langchain4j-cdi-config create `src/main/resources/META-INF/microprofile-config.properties`,
for Quarkus add properties in application.properties.

```properties
# Chat model configuration (Ollama example)
dev.langchain4j.cdi.plugin.chat-model.class=dev.langchain4j.model.ollama.OllamaChatModel
dev.langchain4j.cdi.plugin.chat-model.config.base-url=http://localhost:11434
dev.langchain4j.cdi.plugin.chat-model.config.model-name=llama3.1
dev.langchain4j.cdi.plugin.chat-model.config.temperature=0.7
```

#### Anthropic Claude Example

```properties
dev.langchain4j.cdi.plugin.chat-model.class=dev.langchain4j.model.anthropic.AnthropicChatModel
dev.langchain4j.cdi.plugin.chat-model.config.api-key=${ANTHROPIC_API_KEY}
dev.langchain4j.cdi.plugin.chat-model.config.model-name=claude-sonnet-4-20250514
dev.langchain4j.cdi.plugin.chat-model.config.max-tokens=4096
```

#### Alternative: Using CDI @Produces

You can skip property-based configuration entirely and create your ChatModel using a CDI producer method:

```java
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class ChatModelProducer {

    @Produces
    @ApplicationScoped
    public ChatModel chatModel() {
        return AnthropicChatModel.builder()
                .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                .modelName("claude-sonnet-4-20250514")
                .maxTokens(4096)
                .build();
    }
}
```

This approach gives you full programmatic control over the model configuration.

### 3. Define an AI Service

```java
import dev.langchain4j.cdi.spi.RegisterAIService;
import dev.langchain4j.service.SystemMessage;

@RegisterAIService
public interface AssistantService {

    @SystemMessage("You are a helpful assistant.")
    String chat(String userMessage);
}
```

### 4. Inject and Use

```java
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

@Path("/assistant")
public class AssistantResource {

    @Inject
    AssistantService assistant;

    @GET
    @Path("/chat")
    public String chat(@QueryParam("message") String message) {
        return assistant.chat(message);
    }
}
```

---

## Dependencies

### Module Overview

| Module | Purpose |
|--------|---------|
| `langchain4j-cdi-core` | Core CDI integration classes |
| `langchain4j-cdi-portable-ext` | Runtime CDI extension |
| `langchain4j-cdi-build-compatible-ext` | Build-time CDI extension |
| `langchain4j-cdi-config` | MicroProfile Config integration (also provides `${...}` expression resolver) |
| `langchain4j-cdi-el` | Jakarta EL expression resolver for `#{...}` expressions in annotation attributes |
| `langchain4j-cdi-fault-tolerance` | MicroProfile Fault Tolerance support |
| `langchain4j-cdi-telemetry` | OpenTelemetry metrics for AI operations |
| `langchain4j-agentic` | LangChain4j agentic framework (required for `@RegisterAgent`) |
| `langchain4j-agentic-a2a` | A2A protocol support (required for A2A topology) |

### Optional MicroProfile Modules

```xml
<!-- For fault tolerance (@Retry, @Timeout, @CircuitBreaker) -->
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-fault-tolerance</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>

<!-- For OpenTelemetry metrics -->
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-telemetry</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>

<!-- To use microprofile configurations -->
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-config</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>

<!-- To resolve Jakarta EL expressions #{...} in annotation attributes -->
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-el</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>
```

---

## Configuration Reference

### Configuration Architecture

LangChain4j CDI uses a **Service Provider Interface (SPI)** pattern for configuration, making it portable across different configuration systems.

#### The LLMConfig SPI

At the core is the abstract `LLMConfig` class (`dev.langchain4j.cdi.core.config.spi.LLMConfig`). This class defines three abstract methods that any configuration provider must implement:

```java
public abstract class LLMConfig {

    public static final String PREFIX = "dev.langchain4j.cdi.plugin";

    /** Initialize the configuration source (called once at startup) */
    public abstract void init();

    /** Return all property keys available in the configuration */
    public abstract Set<String> getPropertyKeys();

    /** Return the value for a given property key, or null if not found */
    public abstract String getValue(String key);
}
```

The `LLMConfig` implementation is discovered via **Java ServiceLoader**. You register your implementation in:

```
META-INF/services/dev.langchain4j.cdi.core.config.spi.LLMConfig
```

#### Default Implementation: MicroProfile Config

The `langchain4j-cdi-config` module provides `LLMConfigMPConfig`, an implementation that uses **MicroProfile Config**:

```java
public class LLMConfigMPConfig extends LLMConfig {

    private Config config;

    @Override
    public void init() {
        config = ConfigProvider.getConfig();
    }

    @Override
    public Set<String> getPropertyKeys() {
        return StreamSupport.stream(config.getPropertyNames().spliterator(), false)
                .filter(prop -> prop.startsWith(PREFIX))
                .collect(Collectors.toSet());
    }

    @Override
    public String getValue(String key) {
        return config.getOptionalValue(key, String.class).orElse(null);
    }
}
```

This is registered in the service file:
```
# META-INF/services/dev.langchain4j.cdi.core.config.spi.LLMConfig
dev.langchain4j.cdi.core.mpconfig.LLMConfigMPConfig
```

**When you add `langchain4j-cdi-config` to your dependencies, this implementation is automatically used.**

#### Custom Configuration Provider

You can create your own `LLMConfig` implementation for other configuration sources (YAML, database, etc.):

```java
public class YamlLLMConfig extends LLMConfig {

    private Map<String, String> properties;

    @Override
    public void init() {
        // Load from YAML file
        properties = loadYamlConfig("llm-config.yaml");
    }

    @Override
    public Set<String> getPropertyKeys() {
        return properties.keySet();
    }

    @Override
    public String getValue(String key) {
        return properties.get(key);
    }
}
```

Register it in `META-INF/services/dev.langchain4j.cdi.core.config.spi.LLMConfig`:
```
com.example.YamlLLMConfig
```

---

### Configuration Property Format

All LangChain4j components are configured using the following pattern:

```properties
dev.langchain4j.cdi.plugin.<bean-name>.class=<fully.qualified.ClassName>
dev.langchain4j.cdi.plugin.<bean-name>.scope=<scope-annotation>  # Optional, defaults to @ApplicationScoped
dev.langchain4j.cdi.plugin.<bean-name>.config.<property>=<value>
```

The `<bean-name>` becomes the CDI bean name (used with `@Named` qualifier).

The `<property>` is the property name camel cased converted to dashed text : logResponses -> log-responses.

### Chat Models

#### Ollama

```properties
dev.langchain4j.cdi.plugin.chat-model.class=dev.langchain4j.model.ollama.OllamaChatModel
dev.langchain4j.cdi.plugin.chat-model.config.base-url=http://localhost:11434
dev.langchain4j.cdi.plugin.chat-model.config.model-name=llama3.1
dev.langchain4j.cdi.plugin.chat-model.config.temperature=0.7
dev.langchain4j.cdi.plugin.chat-model.config.timeout=PT60S
dev.langchain4j.cdi.plugin.chat-model.config.log-requests=true
dev.langchain4j.cdi.plugin.chat-model.config.log-responses=true
```

#### OpenAI

```properties
dev.langchain4j.cdi.plugin.chat-model.class=dev.langchain4j.model.openai.OpenAiChatModel
dev.langchain4j.cdi.plugin.chat-model.config.api-key=${OPENAI_API_KEY}
dev.langchain4j.cdi.plugin.chat-model.config.model-name=gpt-4
dev.langchain4j.cdi.plugin.chat-model.config.temperature=0.7
dev.langchain4j.cdi.plugin.chat-model.config.max-tokens=1000
```

#### Azure OpenAI

```properties
dev.langchain4j.cdi.plugin.chat-model.class=dev.langchain4j.model.azure.AzureOpenAiChatModel
dev.langchain4j.cdi.plugin.chat-model.config.endpoint=${AZURE_OPENAI_ENDPOINT}
dev.langchain4j.cdi.plugin.chat-model.config.api-key=${AZURE_OPENAI_KEY}
dev.langchain4j.cdi.plugin.chat-model.config.deployment-name=gpt-4
```

#### Anthropic Claude

```properties
dev.langchain4j.cdi.plugin.chat-model.class=dev.langchain4j.model.anthropic.AnthropicChatModel
dev.langchain4j.cdi.plugin.chat-model.config.api-key=${ANTHROPIC_API_KEY}
dev.langchain4j.cdi.plugin.chat-model.config.model-name=claude-sonnet-4-20250514
dev.langchain4j.cdi.plugin.chat-model.config.max-tokens=4096
dev.langchain4j.cdi.plugin.chat-model.config.temperature=0.7
dev.langchain4j.cdi.plugin.chat-model.config.log-requests=true
dev.langchain4j.cdi.plugin.chat-model.config.log-responses=true
```

### Chat Memory

```properties
# MessageWindowChatMemory - keeps last N messages
dev.langchain4j.cdi.plugin.my-memory.class=dev.langchain4j.memory.chat.MessageWindowChatMemory
dev.langchain4j.cdi.plugin.my-memory.scope=jakarta.enterprise.context.ApplicationScoped
dev.langchain4j.cdi.plugin.my-memory.config.maxMessages=20
```

### Content Retriever (RAG)

```properties
dev.langchain4j.cdi.plugin.my-retriever.class=dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
dev.langchain4j.cdi.plugin.my-retriever.config.embeddingStore=lookup:@default
dev.langchain4j.cdi.plugin.my-retriever.config.embeddingModel=lookup:@default
dev.langchain4j.cdi.plugin.my-retriever.config.maxResults=5
dev.langchain4j.cdi.plugin.my-retriever.config.minScore=0.7
```

### Special Lookup Values

When a configuration property expects another CDI bean, use the `lookup:` prefix:

| Value | Description |
|-------|-------------|
| `lookup:@default` | Use the default (unqualified) CDI bean |
| `lookup:@all` | Inject all beans as a List |
| `lookup:<bean-name>` | Use the bean with `@Named("<bean-name>")` |

---

## AI Service Registration

### @RegisterAIService Annotation

```java
@RegisterAIService(
    scope = RequestScoped.class,           // CDI scope (default: RequestScoped)
    tools = {BookingTools.class},          // Tool classes for function calling
    chatModelName = "chat-model",          // Name of ChatModel bean (default: "#default")
    chatMemoryName = "my-memory",          // Name of ChatMemory bean
    contentRetrieverName = "my-retriever", // Name of ContentRetriever bean (for RAG)
    retrievalAugmentorName = "",           // Alternative to contentRetriever
    moderationModelName = "",              // Name of ModerationModel bean
    streamingChatModelName = "",           // Name of StreamingChatModel bean
    chatMemoryProviderName = "",           // Name of ChatMemoryProvider bean
    toolProviderName = ""                  // Name of ToolProvider bean
)
public interface MyAiService {
    // ...
}
```

### Attribute Reference

| Attribute | Default | Description |
|-----------|---------|-------------|
| `scope` | `RequestScoped.class` | CDI scope for the AI service bean |
| `tools` | `{}` | Array of CDI bean classes containing `@Tool` methods |
| `chatModelName` | `"#default"` | Name of the ChatModel bean. `"#default"` uses the default bean. Supports [expressions](#expression-resolvers) |
| `chatMemoryName` | `""` | Name of ChatMemory bean (empty = no memory). Supports [expressions](#expression-resolvers) |
| `contentRetrieverName` | `""` | Name of ContentRetriever bean for RAG. Supports [expressions](#expression-resolvers) |
| `retrievalAugmentorName` | `""` | Name of RetrievalAugmentor bean (alternative to contentRetriever). Supports [expressions](#expression-resolvers) |
| `moderationModelName` | `""` | Name of ModerationModel bean for content moderation. Supports [expressions](#expression-resolvers) |
| `streamingChatModelName` | `""` | Name of StreamingChatModel bean for streaming responses. Supports [expressions](#expression-resolvers) |
| `chatMemoryProviderName` | `""` | Name of ChatMemoryProvider bean (for per-user memory). Supports [expressions](#expression-resolvers) |
| `toolProviderName` | `""` | Name of ToolProvider bean (dynamic tool discovery). Supports [expressions](#expression-resolvers) |

### LangChain4j Annotations

Use standard LangChain4j annotations on your AI service methods:

```java
@RegisterAIService
public interface MyAiService {

    @SystemMessage("You are a helpful assistant specialized in {{topic}}.")
    @UserMessage("Answer this question: {{question}}")
    String ask(@V("topic") String topic, @V("question") String question);

    @SystemMessage("Summarize the following text.")
    String summarize(@UserMessage String text);
}
```

---

## Guardrails

Guardrails provide input and output validation for AI service interactions, allowing you to enforce rules, validate content, and ensure safe AI operations. They can be configured at the class or method level.

### Basic Usage

Configure guardrails using the `@RegisterAIService` annotation:

```java
@RegisterAIService(
    chatModelName = "chat-model",
    inputGuardrails = {NoEmptyMessageGuardrail.class},
    outputGuardrails = {ContentFilterGuardrail.class}
)
public interface SafeAssistant {
    String chat(String message);
}
```

### CDI Integration

Guardrails are resolved as CDI beans, enabling full dependency injection support:

```java
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class NoEmptyMessageGuardrail implements InputGuardrail {
    
    @Inject
    Logger logger;
    
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String text = userMessage.singleText();
        if (text == null || text.isBlank()) {
            logger.warning("Empty message rejected by guardrail");
            return fatal("Message must not be empty");
        }
        return success();
    }
}
```

### Output Guardrails

Output guardrails validate AI responses before returning them to the user:

```java
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ContentFilterGuardrail implements OutputGuardrail {

    private static final List<String> FORBIDDEN_WORDS = List.of("forbidden", "blocked");
    
    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        String text = responseFromLLM.text();
        
        for (String word : FORBIDDEN_WORDS) {
            if (text.toLowerCase().contains(word)) {
                return reprompt("Response contains forbidden content. Please rephrase.");
            }
        }
        
        return success();
    }
}
```

### Method-Level Guardrails

Override class-level guardrails for specific methods using `@InputGuardrails` and `@OutputGuardrails` annotations:

```java
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.guardrail.OutputGuardrails;

@RegisterAIService(
    chatModelName = "chat-model",
    inputGuardrails = {BasicGuardrail.class}  // Applied to all methods by default
)
public interface FlexibleAssistant {
    
    String chat(String message);  // Uses BasicGuardrail
    
    @InputGuardrails(StrictGuardrail.class)  // Overrides class-level for this method
    @OutputGuardrails(ContentFilterGuardrail.class)
    String sensitiveChat(String message);
}
```

### Fallback Behavior

If a guardrail class is not available as a CDI bean, the framework will attempt to instantiate it using the no-arg constructor:

```java
// This guardrail doesn't need to be a CDI bean
public class SimpleGuardrail implements InputGuardrail {
    
    public SimpleGuardrail() {
        // No-arg constructor required for fallback
    }
    
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        return success();
    }
}
```

### Configuration via Named CDI Beans

You can also reference guardrails by CDI bean name using `@Named`:

```java
@ApplicationScoped
@Named("customInputGuardrail")
public class CustomInputGuardrail implements InputGuardrail {

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        // your validation logic
        return success();
    }
}
```

```java
@RegisterAIService(
    chatModelName = "chat-model",
    inputGuardrailNames = {"customInputGuardrail"},
    outputGuardrailNames = {"customOutputGuardrail"}
)
public interface NamedGuardrailService {
    String chat(String message);
}
```

**Note:** If both `inputGuardrails` (classes) and `inputGuardrailNames` (bean names) are specified, the classes take precedence and names are ignored. A warning will be logged.

### Guardrail Results

Guardrails can return different result types:

| Result Type | Method | Description |
|-------------|--------|-------------|
| Success | `success()` | Validation passed, continue processing |
| Fatal | `fatal(String message)` | Validation failed, abort with error |
| Reprompt | `reprompt(String message)` | For output guardrails: retry with modified prompt |

### Output Guardrail Retries

Configure maximum retry attempts for output guardrails:

```java
import dev.langchain4j.service.guardrail.OutputGuardrails;

@RegisterAIService
public interface RetryableAssistant {
    
    @OutputGuardrails(value = ContentFilterGuardrail.class, maxRetries = 3)
    String chat(String message);
}
```

### Guardrail Configuration Properties

Input and output guardrails have separate configuration objects:

| Config Class | Properties | Description |
|-------------|-----------|-------------|
| `InputGuardrailsConfig` | *(none)* | Input guardrails have no configurable properties. Failures are passed directly to the caller as a `GuardrailException`. |
| `OutputGuardrailsConfig` | `maxRetries` (default: 2) | Maximum retry attempts when an output guardrail triggers a retry or reprompt. Set to `0` to disable retries. |

### Method-Level vs Class-Level Guardrails

Guardrails can be applied at both the class level (via `@RegisterAIService`) and the method level (via `@InputGuardrails` / `@OutputGuardrails` annotations from LangChain4j). When both are present, **method-level annotations override class-level settings** for that specific method:

```java
@RegisterAIService(
    chatModelName = "chat-model",
    inputGuardrails = {BasicGuardrail.class}  // Applied to methods without overrides
)
@OutputGuardrails(ContentFilterGuardrail.class)  // Class-level output guardrail
public interface FlexibleAssistant {

    String chat(String message);  // Uses BasicGuardrail (input) + ContentFilterGuardrail (output)

    @InputGuardrails(StrictGuardrail.class)  // Overrides class-level input guardrail
    String sensitiveChat(String message);    // Uses StrictGuardrail (input) + ContentFilterGuardrail (output)
}
```

### Best Practices

1. **Use appropriate CDI scopes:**
   - `@ApplicationScoped` for stateless guardrails (recommended)
   - `@RequestScoped` for guardrails that need request-specific data

2. **Keep guardrails focused:** Each guardrail should validate one specific concern

3. **Provide clear error messages:** Use descriptive messages in `fatal()` and `reprompt()` results

4. **Consider performance:** Guardrails are executed on every AI service call

5. **Test thoroughly:** Write unit tests for your guardrail logic

6. **Log appropriately:** Use injected loggers to track guardrail decisions

### Example: Complete Guardrail Implementation

```java
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class MessageLengthGuardrail implements InputGuardrail {
    
    private static final int MAX_LENGTH = 1000;
    private static final int MIN_LENGTH = 5;
    
    @Inject
    Logger logger;
    
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String text = userMessage.singleText();
        
        if (text == null || text.length() < MIN_LENGTH) {
            logger.warning("Message too short: " + (text != null ? text.length() : 0) + " characters");
            return fatal("Message must be at least " + MIN_LENGTH + " characters long");
        }
        
        if (text.length() > MAX_LENGTH) {
            logger.warning("Message too long: " + text.length() + " characters");
            return fatal("Message must not exceed " + MAX_LENGTH + " characters");
        }
        
        logger.fine("Message length validated: " + text.length() + " characters");
        return success();
    }
}
```

---

## Agent Registration

The `@RegisterAgent` annotation enables declarative registration of [LangChain4j Agentic](https://docs.langchain4j.dev/tutorials/agentic) systems as CDI beans. While `@RegisterAIService` creates simple AI services backed by a single LLM call, `@RegisterAgent` creates agentic systems that can orchestrate multiple sub-agents in workflows such as sequences, loops, parallel execution, and supervisor-based delegation.

### Dependencies

Add the agentic dependency alongside the CDI extension you already use:

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-agentic</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
```

For A2A (Agent-to-Agent) topology, also add:

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-agentic-a2a</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
```

### @RegisterAgent Annotation

```java
@RegisterAgent(
    name = "my-agent",                         // CDI bean name and agent logical name
    description = "A helpful agent",           // Agent description for orchestrators
    topology = AgentTopologyType.SIMPLE,       // Agent topology (default: SIMPLE)
    scope = ApplicationScoped.class,           // CDI scope (default: ApplicationScoped)
    chatModelName = "#default",                // Name of ChatModel bean
    tools = {MyTools.class},                   // Tool classes
    chatMemoryName = "my-memory",              // Name of ChatMemory bean
    outputKey = "result"                       // Key for storing output in agentic scope
)
public interface MyAgent {

    @Agent(description = "Process the request")
    String process(@V("input") String input);
}
```

### Attribute Reference

| Attribute | Default | Description |
|-----------|---------|-------------|
| `name` | `""` | CDI bean name and agent logical name. When set, the bean is registered with `@Named` and can be referenced as a sub-agent by other agents |
| `description` | `""` | Agent description, used by supervisors and planners to select agents |
| `topology` | `SIMPLE` | Agent topology type (see [Topologies](#topologies)) |
| `scope` | `ApplicationScoped.class` | CDI scope for the agent bean |
| `outputKey` | `""` | Key used to store this agent's output in the shared agentic scope |
| `async` | `false` | Whether the agent runs asynchronously |
| `subAgentNames` | `{}` | CDI bean names of sub-agents for composed topologies |
| `maxIterations` | `10` | Maximum loop iterations (LOOP topology only) |
| `maxAgentsInvocations` | `10` | Maximum agent invocations (SUPERVISOR topology only) |
| `chatModelName` | `"#default"` | Name of the ChatModel bean. `"#default"` uses the default bean |
| `streamingChatModelName` | `""` | Name of StreamingChatModel bean |
| `tools` | `{}` | Array of tool classes containing `@Tool` methods |
| `toolProviderName` | `""` | Name of ToolProvider bean (dynamic tool discovery) |
| `chatMemoryName` | `""` | Name of ChatMemory bean |
| `chatMemoryProviderName` | `""` | Name of ChatMemoryProvider bean |
| `contentRetrieverName` | `""` | Name of ContentRetriever bean for RAG |
| `retrievalAugmentorName` | `""` | Name of RetrievalAugmentor bean |
| `inputGuardrails` | `{}` | Input guardrail classes |
| `outputGuardrails` | `{}` | Output guardrail classes |
| `inputGuardrailNames` | `{}` | Named CDI beans implementing InputGuardrail |
| `outputGuardrailNames` | `{}` | Named CDI beans implementing OutputGuardrail |
| `agentListenerName` | `""` | Name of AgentListener bean for observability |
| `a2aServerUrl` | `""` | URL of the A2A server (A2A topology only) |

### Name Resolution

Bean name resolution follows the same conventions as `@RegisterAIService`:

| Value | Behavior |
|-------|----------|
| `"#default"` | Use the default (unqualified) CDI bean |
| `""` (empty) | Ignore this dependency |
| `"my-bean"` | Use the bean with `@Named("my-bean")` |
| `"${prop.key}"` | Resolve via MicroProfile Config (requires `langchain4j-cdi-config`) |
| `"#{el.expression}"` | Evaluate as Jakarta EL expression (requires `langchain4j-cdi-el`) |

When `name` is set on `@RegisterAgent`, the agent bean is automatically registered with that `@Named` qualifier, making it injectable by name without a separate `@Named` annotation on the interface.

String attributes that support expressions include: `name`, `description`, `outputKey`, `chatModelName`, `streamingChatModelName`, `toolProviderName`, `chatMemoryName`, `chatMemoryProviderName`, `contentRetrieverName`, `retrievalAugmentorName`, `agentListenerName`, and each element of `subAgentNames`.

### Topologies

The `topology` attribute determines how the agent system is built. Each topology maps to a builder in `AgenticServices`.

#### SIMPLE (default)

A single agent backed by a chat model. Equivalent to `@RegisterAIService` but with agent-specific features (output key, listener, agentic scope).

```java
@RegisterAgent(
    name = "style-editor",
    chatModelName = "ollama",
    outputKey = "story")
public interface StyleEditor {

    @UserMessage("Rewrite the following story in {{style}} style: {{story}}")
    @Agent(description = "Edit a story to match a given style", outputKey = "story")
    String editStory(@V("story") String story, @V("style") String style);
}
```

#### SEQUENCE

Executes sub-agents one after another in order. Each agent can read previous agents' outputs from the shared scope via their output keys.

```java
@RegisterAgent(
    name = "content-pipeline",
    topology = AgentTopologyType.SEQUENCE,
    subAgentNames = {"researcher", "writer", "reviewer"},
    outputKey = "final-content")
public interface ContentPipeline {

    @Agent
    String produceContent(@V("topic") String topic);
}
```

#### LOOP

Repeats sub-agents until an exit condition is met or `maxIterations` is reached.

```java
@RegisterAgent(
    name = "refinement-loop",
    topology = AgentTopologyType.LOOP,
    subAgentNames = {"scorer", "editor"},
    maxIterations = 5)
public interface RefinementLoop {

    @Agent
    String refine(@V("draft") String draft);
}
```

#### PARALLEL

Executes all sub-agents concurrently and merges their results.

```java
@RegisterAgent(
    name = "multi-analyst",
    topology = AgentTopologyType.PARALLEL,
    subAgentNames = {"technical-analyst", "business-analyst", "risk-analyst"},
    outputKey = "analysis")
public interface MultiAnalyst {

    @Agent
    String analyze(@V("proposal") String proposal);
}
```

#### CONDITIONAL

Routes to a specific sub-agent based on the input conditions.

```java
@RegisterAgent(
    name = "router",
    topology = AgentTopologyType.CONDITIONAL,
    subAgentNames = {"support-agent", "sales-agent", "billing-agent"})
public interface RequestRouter {

    @Agent
    String route(@V("request") String request);
}
```

#### SUPERVISOR

An LLM-driven orchestrator that dynamically selects which sub-agents to invoke based on the task. Requires a `ChatModel` and uses agent descriptions to make routing decisions.

```java
@RegisterAgent(
    name = "project-manager",
    topology = AgentTopologyType.SUPERVISOR,
    chatModelName = "gpt4",
    subAgentNames = {"developer", "tester", "documenter"},
    maxAgentsInvocations = 15)
public interface ProjectManager {

    @Agent
    String manageTask(@V("task") String task);
}
```

#### PLANNER

Similar to SUPERVISOR but creates an explicit plan before execution, then follows the plan step by step.

```java
@RegisterAgent(
    name = "project-planner",
    topology = AgentTopologyType.PLANNER,
    subAgentNames = {"researcher", "developer", "reviewer"})
public interface ProjectPlanner {

    @Agent
    String executeProject(@V("requirements") String requirements);
}
```

#### A2A (Agent-to-Agent Protocol)

Connects to a remote agent via the [A2A protocol](https://google.github.io/A2A/). The remote agent runs as a separate service and is accessed over HTTP. No `ChatModel` is needed locally.

```java
@RegisterAgent(
    name = "creative-writer",
    topology = AgentTopologyType.A2A,
    a2aServerUrl = "http://localhost:8080",
    outputKey = "story")
public interface RemoteWriter {

    @Agent(description = "Generate a story based on the given topic", outputKey = "story")
    String generateStory(@V("topic") String topic);
}
```

### Composing Agents

Agents registered with `@RegisterAgent` can be composed into larger workflows by referencing them as sub-agents via their `name`:

```java
// Step 1: A remote A2A agent that writes stories
@RegisterAgent(
    name = "creative-writer",
    topology = AgentTopologyType.A2A,
    a2aServerUrl = "http://localhost:8080",
    outputKey = "story")
public interface CreativeWriter {
    @Agent(description = "Generate a Norse saga", outputKey = "story")
    String generateStory(@V("topic") String topic);
}

// Step 2: A local agent that edits stories
@RegisterAgent(
    name = "style-editor",
    chatModelName = "ollama",
    outputKey = "story")
public interface StyleEditor {
    @UserMessage("Rewrite this saga in {{style}} style: {{story}}")
    @Agent(description = "Edit a saga to match a style", outputKey = "story")
    String editStory(@V("story") String story, @V("style") String style);
}

// Step 3: A sequence that chains them together
@RegisterAgent(
    topology = AgentTopologyType.SEQUENCE,
    subAgentNames = {"creative-writer", "style-editor"},
    outputKey = "story")
public interface StyledWriter {
    @Agent
    String writeStoryWithStyle(@V("topic") String topic, @V("style") String style);
}
```

Sub-agents can be any combination of `@RegisterAgent` beans and manually produced CDI beans (via `@Produces @Named`). This is useful for agents that need programmatic configuration, such as a loop with a custom exit condition:

```java
@ApplicationScoped
public class AgentProducers {

    @Inject @Named("scorer") Scorer scorer;
    @Inject StyleEditor styleEditor;

    @Produces
    @Named("review-loop")
    public UntypedAgent reviewLoop() {
        return AgenticServices.loopBuilder()
                .subAgents(
                    CommonAgentCreator.toAgentExecutor(scorer),
                    CommonAgentCreator.toAgentExecutor(styleEditor))
                .maxIterations(5)
                .exitCondition(scope -> {
                    Object score = scope.readState("score");
                    return score instanceof Number n && n.doubleValue() >= 0.8;
                })
                .build();
    }
}
```

**Important:** When passing `@RegisterAgent` CDI beans directly to programmatic agentic builders (like `loopBuilder().subAgents(...)`), wrap them with `CommonAgentCreator.toAgentExecutor()`. CDI client proxies (used for `@ApplicationScoped` beans) lose method annotations, preventing the agentic framework from finding the agent's entry point. This wrapper inspects the original interface instead. This is not needed when using `subAgentNames` in `@RegisterAgent` — the framework handles it automatically.

### Differences from @RegisterAIService

| Feature | `@RegisterAIService` | `@RegisterAgent` |
|---------|---------------------|-------------------|
| Default scope | `RequestScoped` | `ApplicationScoped` |
| Moderation model | Supported | Not supported |
| Agent topologies | N/A | SIMPLE, SEQUENCE, LOOP, PARALLEL, CONDITIONAL, SUPERVISOR, PLANNER, A2A |
| Sub-agent orchestration | N/A | Via `subAgentNames` |
| Output key / agentic scope | N/A | Supported |
| Agent listener | N/A | Via `agentListenerName` |
| CDI bean naming | Requires separate `@Named` | Built-in via `name` attribute |
| A2A protocol | N/A | Via `a2aServerUrl` |

---

## Tools and Function Calling

Tools enable your AI service to call your business logic.

### 1. Define a Tool Class

```java
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BookingTools {

    @Tool("Get all bookings for a customer by their name")
    public String getBookings(@P("Customer full name") String customerName) {
        // Your business logic here
        return "Booking #123: Rental car from 2024-01-15 to 2024-01-20";
    }

    @Tool("Cancel a booking by its ID")
    public String cancelBooking(
            @P("The booking ID to cancel") String bookingId,
            @P("Customer name for verification") String customerName) {
        // Your business logic here
        return "Booking " + bookingId + " has been cancelled.";
    }
}
```

### 2. Register the Tool

```java
@RegisterAIService(tools = BookingTools.class)
public interface BookingAssistant {

    @SystemMessage("""
        You are a booking assistant for a car rental company.
        Use the available tools to help customers with their bookings.
        Always verify customer identity before making changes.
        """)
    String chat(String userMessage);
}
```

### Multiple Tool Classes

```java
@RegisterAIService(tools = {BookingTools.class, PaymentTools.class, NotificationTools.class})
public interface FullServiceAssistant {
    String chat(String message);
}
```

---

## RAG (Retrieval Augmented Generation)

RAG enables your AI to answer questions based on your documents.

### 1. Produce Embedding Components

```java
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class EmbeddingProducers {

    @Produces
    @ApplicationScoped
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Produces
    @ApplicationScoped
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
}
```

### 2. Configure Content Retriever

```properties
dev.langchain4j.cdi.plugin.doc-retriever.class=dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
dev.langchain4j.cdi.plugin.doc-retriever.config.embeddingStore=lookup:@default
dev.langchain4j.cdi.plugin.doc-retriever.config.embeddingModel=lookup:@default
dev.langchain4j.cdi.plugin.doc-retriever.config.maxResults=5
dev.langchain4j.cdi.plugin.doc-retriever.config.minScore=0.6
```

### 3. Use in AI Service

```java
@RegisterAIService(contentRetrieverName = "doc-retriever")
public interface DocumentAssistant {

    @SystemMessage("Answer questions based on the provided context. If unsure, say so.")
    String askAboutDocuments(String question);
}
```

### 4. Load Documents at Startup

```java
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class DocumentLoader {

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @Inject
    EmbeddingModel embeddingModel;

    @PostConstruct
    void loadDocuments() {
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("docs/");

        EmbeddingStoreIngestor.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .documentSplitter(DocumentSplitters.recursive(500, 50))
            .build()
            .ingest(documents);
    }
}
```

---

## Chat Memory

Chat memory maintains conversation context across multiple interactions.

### Application-Scoped Memory (Shared)

```properties
dev.langchain4j.cdi.plugin.shared-memory.class=dev.langchain4j.memory.chat.MessageWindowChatMemory
dev.langchain4j.cdi.plugin.shared-memory.scope=jakarta.enterprise.context.ApplicationScoped
dev.langchain4j.cdi.plugin.shared-memory.config.maxMessages=50
```

```java
@RegisterAIService(chatMemoryName = "shared-memory")
public interface ChatBot {
    String chat(String message);
}
```

### Per-User Memory with ChatMemoryProvider

For multi-user scenarios, use a `ChatMemoryProvider`:

```java
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@Named("per-user-memory")
public class UserChatMemoryProvider implements ChatMemoryProvider {

    private final Map<Object, ChatMemory> memories = new ConcurrentHashMap<>();

    @Override
    public ChatMemory get(Object memoryId) {
        return memories.computeIfAbsent(memoryId,
            id -> MessageWindowChatMemory.withMaxMessages(20));
    }
}
```

```java
@RegisterAIService(chatMemoryProviderName = "per-user-memory")
public interface UserChatBot {

    @SystemMessage("You are a helpful assistant.")
    String chat(@MemoryId String sessionId, @UserMessage String message);
}
```

---

## Expression Resolvers

Annotation string attributes on `@RegisterAIService` and `@RegisterAgent` (such as `chatModelName`, `name`, `description`, `subAgentNames`, `outputKey`, and all `*Name` bean-lookup fields) can contain expressions that are resolved at runtime rather than hardcoded. This lets you externalise any bean name through configuration or compute it dynamically.

Resolution is done via the `ExpressionResolver` SPI (`dev.langchain4j.cdi.spi.ExpressionResolver`). Implementations are discovered via `java.util.ServiceLoader` and applied as a pipeline: the output of each resolver feeds into the next.

### MicroProfile Config Expressions — `${...}`

When `langchain4j-cdi-config` is on the classpath, any attribute value matching `${property.key}` is resolved through MicroProfile Config:

```xml
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-config</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>
```

```java
@RegisterAIService(chatModelName = "${my.chat.model.name}")
public interface AssistantService {
    String chat(String message);
}
```

```properties
# microprofile-config.properties (or application.properties in Quarkus)
my.chat.model.name=chat-model
```

If the key is not found, the original `${property.key}` string is returned unchanged and a `FINE` log message is emitted so the problem is visible.

### Jakarta EL Expressions — `#{...}`

When `langchain4j-cdi-el` is on the classpath, any attribute value matching `#{expression}` is evaluated as a Jakarta Expression Language expression:

```xml
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-el</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>
```

You also need a Jakarta EL runtime. In managed environments (WildFly, Payara, GlassFish, Liberty, Quarkus) one is already provided. In standalone or test scenarios add expressly explicitly:

```xml
<dependency>
    <groupId>org.glassfish.expressly</groupId>
    <artifactId>expressly</artifactId>
    <version>5.0.0</version>
    <scope>runtime</scope>
</dependency>
```

```java
// Conditional bean name
@RegisterAIService(chatModelName = "#{config.useGpu ? 'gpu-model' : 'cpu-model'}")
public interface SmartAssistant {
    String chat(String message);
}

// Reference a CDI @Named bean property
@RegisterAgent(name = "#{agentConfig.reviewerName}", chatModelName = "#default")
public interface ReviewAgent {
    @Agent String review(@V("text") String text);
}
```

When CDI is active, `@Named` beans are accessible directly by name in EL expressions (e.g. `#{myConfig.propertyName}`). If EL evaluation fails for any reason, the original `#{...}` expression is returned unchanged and a `WARNING` is logged.

### Using Both Together

Both modules can coexist. The resolvers run in pipeline order: MP Config first, then Jakarta EL (or in whichever order `ServiceLoader` discovers them). The `${...}` and `#{...}` delimiters are distinct so there is no conflict.

### Custom Expression Resolver

Implement `ExpressionResolver` and register it via `ServiceLoader` to add your own syntax or resolution strategy:

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

---

## MicroProfile Integration

### Fault Tolerance

Add resilience to AI operations:

```xml
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-fault-tolerance</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>
```

```java
import org.eclipse.microprofile.faulttolerance.*;
import java.time.temporal.ChronoUnit;

@RegisterAIService
public interface ResilientAssistant {

    @Retry(maxRetries = 3, delay = 1000)
    @Timeout(value = 30, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5)
    @Fallback(fallbackMethod = "fallbackChat")
    String chat(String message);

    default String fallbackChat(String message) {
        return "I'm temporarily unavailable. Please try again later.";
    }
}
```

### Telemetry

Monitor AI operations with OpenTelemetry:

```xml
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-telemetry</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>
```

Collected metrics:
- `gen_ai.client.token.usage` - Input/output token counts
- `gen_ai.client.operation.duration` - Operation duration in seconds

---

## Examples

All examples are based on a "Miles of Smiles" car rental company application inspired from the [Java meets AI](https://www.youtube.com/watch?v=BD1MSLbs9KE) talk from [Lize Raes](https://www.linkedin.com/in/lize-raes-a8a34110/) at Devoxx Belgium 2023. Each example demonstrates:

- **Chat Service**: Customer assistant with RAG (Retrieval Augmented Generation)
- **Fraud Detection**: AI-powered fraud detection service
- **Function Calling**: Integration with business logic through tools

Complete example applications are available in the `examples/` directory:

| Example | Runtime | Extension | Description |
|---------|---------|-----------|-------------|
| `payara-car-booking` | Payara Micro | Portable | Payara Micro with web UI |
| `liberty-car-booking` | Open Liberty | Portable | Liberty with OpenAPI UI |
| `liberty-car-booking-mcp` | Open Liberty | Portable | Liberty with MCP (Model Context Protocol) |
| `helidon-car-booking` | Helidon 4 | Build-compatible | Helidon with build-time extension |
| `helidon-car-booking-portable-ext` | Helidon 4 | Portable | Helidon with runtime extension |
| `quarkus-car-booking` | Quarkus | Build-compatible | Quarkus dev UI integration |
| `glassfish-car-booking` | GlassFish | Portable | Jakarta EE full profile |

### Running Examples

Each example includes a `run.sh` script that starts Ollama (if needed) and the application server.

**Quick start with any example:**

```bash
cd examples/<example-name>
./run.sh
```

**Manual setup:**

1. **Start Ollama:**

```bash
# Using Docker/Podman
docker run -d --name ollama -p 11434:11434 -v ollama:/root/.ollama ollama/ollama
docker exec -it ollama ollama pull llama3.1

# Or install locally: https://ollama.ai/
ollama pull llama3.1
```

2. **Run the example:**

| Example | Command | Port |
|---------|---------|------|
| `payara-car-booking` | `./run.sh` | 8080 |
| `liberty-car-booking` | `mvn liberty:dev` | 9080 |
| `helidon-car-booking` | `./run.sh` | 8080 |
| `quarkus-car-booking` | `./runexample.sh` | 8080 |
| `glassfish-car-booking` | `./run.sh` | 8080 |

3. **Access the application:**

- **Payara**: http://localhost:8080/
- **Liberty**: http://localhost:9080/openapi/ui
- **Helidon**: http://localhost:8080/openapi/ui
- **Quarkus**: http://localhost:8080/
- **GlassFish**: http://localhost:8080/glassfish-car-booking/api/car-booking/

### Sample Questions

Once running, you can ask questions like:

- "Hello, how can you help me?"
- "What is your cancellation policy?"
- "What is your list of cars?"
- "My name is James Bond, please list my bookings"
- "Is my booking 123-456 cancelable?"

---

## Troubleshooting

### Common Issues

**Bean not found for ChatModel**
- Ensure you have configured the chat model in `microprofile-config.properties`
- Check the bean name matches what you specified in `chatModelName`

**Configuration not loading**
- Verify `microprofile-config.properties` is in `src/main/resources/META-INF/`
- Check property names match the builder method names (use kebab-case: `base-url`, `model-name`)

**Tool methods not being called**
- Ensure tool class is a CDI bean (`@ApplicationScoped`)
- Check `@Tool` description is clear for the LLM to understand when to use it
- Verify tool class is listed in `@RegisterAIService(tools = ...)`

### Debug Logging

Enable request/response logging:

```properties
dev.langchain4j.cdi.plugin.chat-model.config.log-requests=true
dev.langchain4j.cdi.plugin.chat-model.config.log-responses=true
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.

## License

Apache License 2.0 - see [LICENSE](LICENSE) file.
# Agentic Systems with LangChain4j CDI

The `@RegisterAgent` annotation enables declarative registration of [LangChain4j Agentic](https://docs.langchain4j.dev/tutorials/agentic) systems as CDI beans. While `@RegisterAIService` creates simple AI services backed by a single LLM call, `@RegisterAgent` creates agentic systems that can orchestrate multiple sub-agents in workflows such as sequences, loops, parallel execution, and supervisor-based delegation.

## Table of Contents

- [Dependencies](#dependencies)
- [@RegisterAgent Annotation](#registeragent-annotation)
- [Attribute Reference](#attribute-reference)
- [Topology-Attribute Compatibility](#topology-attribute-compatibility)
- [Topologies](#topologies)
- [Composing Agents](#composing-agents)
- [Differences from @RegisterAIService](#differences-from-registeraiservice)
- [The A2AAgentBuilder SPI](#the-a2aagentbuilder-spi)

## Dependencies

Add the agentic dependency alongside the CDI extension you already use:

```xml
<!-- Core agentic support — required for @RegisterAgent -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-agentic</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
```

For **A2A (Agent-to-Agent) topology**, also add the A2A CDI integration module and the A2A protocol library:

```xml
<!-- CDI wiring for A2A topology -->
<dependency>
    <groupId>dev.langchain4j.cdi</groupId>
    <artifactId>langchain4j-cdi-a2a</artifactId>
    <version>${langchain4j-cdi.version}</version>
</dependency>

<!-- A2A protocol implementation -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-agentic-a2a</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
```

`langchain4j-cdi-a2a` is an optional module loaded via `ServiceLoader` when the A2A topology is requested. If it is absent from the classpath and `topology = AgentTopologyType.A2A` is used, an `IllegalStateException` is thrown at startup with a clear message.

## @RegisterAgent Annotation

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

## Attribute Reference

| Attribute | Default | Description |
|-----------|---------|-------------|
| `name` | `""` | CDI bean name and agent logical name. When set, the bean is registered with `@Named` and can be referenced as a sub-agent by other agents |
| `description` | `""` | Agent description, used by supervisors and planners to select agents |
| `topology` | `SIMPLE` | Agent topology type (see [Topologies](#topologies)) |
| `scope` | `ApplicationScoped.class` | CDI scope for the agent bean |
| `outputKey` | `""` | Key used to store this agent's output in the shared agentic scope |
| `async` | `false` | Whether the agent runs asynchronously (SIMPLE and A2A only) |
| `subAgentNames` | `{}` | CDI bean names of sub-agents for composed topologies |
| `maxIterations` | `10` | Maximum loop iterations (LOOP topology only) |
| `maxAgentsInvocations` | `10` | Maximum agent invocations (SUPERVISOR topology only) |
| `chatModelName` | `"#default"` | Name of the ChatModel bean. `"#default"` uses the default bean |
| `streamingChatModelName` | `""` | Name of StreamingChatModel bean (SIMPLE topology only) |
| `tools` | `{}` | Array of tool classes containing `@Tool` methods (SIMPLE topology only) |
| `toolProviderName` | `""` | Name of ToolProvider bean (SIMPLE topology only) |
| `chatMemoryName` | `""` | Name of ChatMemory bean (SIMPLE topology only) |
| `chatMemoryProviderName` | `""` | Name of ChatMemoryProvider bean (SIMPLE and SUPERVISOR topologies) |
| `contentRetrieverName` | `""` | Name of ContentRetriever bean for RAG (SIMPLE topology only) |
| `retrievalAugmentorName` | `""` | Name of RetrievalAugmentor bean (SIMPLE topology only) |
| `inputGuardrails` | `{}` | Input guardrail classes (SIMPLE topology only) |
| `outputGuardrails` | `{}` | Output guardrail classes (SIMPLE topology only) |
| `inputGuardrailNames` | `{}` | Named CDI beans implementing InputGuardrail (SIMPLE topology only) |
| `outputGuardrailNames` | `{}` | Named CDI beans implementing OutputGuardrail (SIMPLE topology only) |
| `agentListenerName` | `""` | Name of AgentListener bean for observability (all topologies) |
| `a2aServerUrl` | `""` | URL of the A2A server (A2A topology only, required) |

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

String attributes that support expression resolution: `name`, `description`, `outputKey`, `chatModelName`, `streamingChatModelName`, `toolProviderName`, `chatMemoryName`, `chatMemoryProviderName`, `contentRetrieverName`, `retrievalAugmentorName`, `agentListenerName`, `a2aServerUrl`, and each element of `subAgentNames`.

## Topology-Attribute Compatibility

Not all attributes are meaningful for every topology. The CDI integration only wires the components that a given topology builder accepts. Attributes marked `—` below are silently ignored for that topology — no warning is emitted.

| Attribute | SIMPLE | SEQ | LOOP | PAR | COND | SUPER | PLAN | A2A |
|-----------|:------:|:---:|:----:|:---:|:----:|:-----:|:----:|:---:|
| `name` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| `description` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| `outputKey` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `scope` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `agentListenerName` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `async` | ✅ | — | — | — | — | — | — | ✅ |
| `subAgentNames` | — | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| `maxIterations` | — | — | ✅ | — | — | — | — | — |
| `maxAgentsInvocations` | — | — | — | — | — | ✅ | — | — |
| `chatModelName` | ✅ | — | — | — | — | ✅ | — | — |
| `streamingChatModelName` | ✅ | — | — | — | — | — | — | — |
| `tools` / `toolProviderName` | ✅ | — | — | — | — | — | — | — |
| `chatMemoryName` | ✅ | — | — | — | — | — | — | — |
| `chatMemoryProviderName` | ✅ | — | — | — | — | ✅ | — | — |
| `contentRetrieverName` | ✅ | — | — | — | — | — | — | — |
| `retrievalAugmentorName` | ✅ | — | — | — | — | — | — | — |
| `inputGuardrails` / `Names` | ✅ | — | — | — | — | — | — | — |
| `outputGuardrails` / `Names` | ✅ | — | — | — | — | — | — | — |
| `a2aServerUrl` | — | — | — | — | — | — | — | ✅ |

Column abbreviations: SEQ = SEQUENCE, PAR = PARALLEL, COND = CONDITIONAL, SUPER = SUPERVISOR, PLAN = PLANNER.

**Notable constraints:**
- `chatMemoryProviderName` is available for both SIMPLE and SUPERVISOR, but not `chatMemoryName` for SUPERVISOR — use `chatMemoryProviderName` when the supervisor needs memory.
- `chatModelName` defaults to `"#default"` for both SIMPLE and SUPERVISOR, so it always tries to resolve the default `ChatModel` bean unless overridden or left explicitly blank.
- `contentRetrieverName` and `retrievalAugmentorName` are mutually exclusive: `retrievalAugmentor` takes precedence when both are specified.

## Topologies

The `topology` attribute determines how the agent system is built. Each topology maps to a builder in `AgenticServices`.

### SIMPLE (default)

A single agent backed by a chat model. Full component wiring — tools, memory, retrieval, guardrails, streaming — is available. Equivalent to `@RegisterAIService` but with agent-specific features (output key, listener, agentic scope).

```java
@RegisterAgent(
    name = "style-editor",
    chatModelName = "ollama",
    tools = {StyleTools.class},
    outputKey = "story")
public interface StyleEditor {

    @UserMessage("Rewrite the following story in {{style}} style: {{story}}")
    @Agent(description = "Edit a story to match a given style", outputKey = "story")
    String editStory(@V("story") String story, @V("style") String style);
}
```

### SEQUENCE

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

### LOOP

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

### PARALLEL

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

### CONDITIONAL

Routes to a specific sub-agent based on input conditions.

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

### SUPERVISOR

An LLM-driven orchestrator that dynamically selects which sub-agents to invoke based on the task. Requires a `ChatModel`. Uses agent descriptions to make routing decisions.

```java
@RegisterAgent(
    name = "project-manager",
    topology = AgentTopologyType.SUPERVISOR,
    chatModelName = "gpt4",
    chatMemoryProviderName = "per-session-memory",
    subAgentNames = {"developer", "tester", "documenter"},
    maxAgentsInvocations = 15)
public interface ProjectManager {

    @Agent
    String manageTask(@V("task") String task);
}
```

**Note:** SUPERVISOR supports `chatMemoryProviderName` (a `ChatMemoryProvider` that creates memory per session). It does **not** support `chatMemoryName` (a single shared `ChatMemory` instance) — use `chatMemoryProviderName` when the supervisor needs memory.

### PLANNER

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

### A2A (Agent-to-Agent Protocol)

Connects to a remote agent via the [A2A protocol](https://google.github.io/A2A/). The remote agent runs as a separate service and is accessed over HTTP. No local `ChatModel` is needed. Requires `langchain4j-cdi-a2a` on the classpath (see [Dependencies](#dependencies)).

```java
@RegisterAgent(
    name = "creative-writer",
    topology = AgentTopologyType.A2A,
    a2aServerUrl = "http://remote-agent-host:8080",
    outputKey = "story")
public interface RemoteWriter {

    @Agent(description = "Generate a story based on the given topic", outputKey = "story")
    String generateStory(@V("topic") String topic);
}
```

`a2aServerUrl` supports expression resolution, so the URL can be externalised:

```java
@RegisterAgent(
    name = "remote-summarizer",
    topology = AgentTopologyType.A2A,
    a2aServerUrl = "${remote.summarizer.url}")
public interface RemoteSummarizer {
    @Agent String summarize(@V("text") String text);
}
```

## Composing Agents

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

**Important:** When passing `@RegisterAgent` CDI beans directly to programmatic agentic builders (like `loopBuilder().subAgents(...)`), wrap them with `CommonAgentCreator.toAgentExecutor()`. CDI client proxies (used for `@ApplicationScoped` beans) lose method annotations, preventing the agentic framework from finding the agent's entry point. This wrapper inspects the original interface instead. This is **not** needed when using `subAgentNames` in `@RegisterAgent` — the framework handles it automatically.

## Differences from @RegisterAIService

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

## The A2AAgentBuilder SPI

The A2A topology is loaded via an SPI so the `langchain4j-cdi-a2a` dependency remains optional. The `A2AAgentBuilder` interface (`dev.langchain4j.cdi.agent.spi.A2AAgentBuilder`) defines the contract:

```java
public interface A2AAgentBuilder {
    <X> X build(Class<X> interfaceClass, RegisterAgent annotation, Instance<Object> lookup);
}
```

`DefaultA2AAgentBuilder` (in this module) is the standard implementation, registered in `META-INF/services/dev.langchain4j.cdi.agent.spi.A2AAgentBuilder`. It resolves `a2aServerUrl` through the expression resolver pipeline, so `${...}` and `#{...}` expressions are supported. It wires `outputKey`, `async`, and `agentListenerName`; all other `@RegisterAgent` attributes are unused for A2A agents.

You can provide a custom `A2AAgentBuilder` implementation (e.g. to add custom HTTP configuration or authentication) by registering it as a ServiceLoader service. If multiple implementations are on the classpath, the first one found is used — the standard Java `ServiceLoader` discovery order applies.

## License

Apache License 2.0 — see the [LICENSE](../LICENSE) file.

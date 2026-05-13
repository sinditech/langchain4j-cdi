package dev.langchain4j.cdi.agent;

import static dev.langchain4j.cdi.aiservice.CdiLookupHelper.hasText;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.agent.AgentBuilder;
import dev.langchain4j.agentic.internal.AgentExecutor;
import dev.langchain4j.agentic.internal.AgentInvoker;
import dev.langchain4j.agentic.internal.AgentUtil;
import dev.langchain4j.agentic.internal.InternalAgent;
import dev.langchain4j.agentic.internal.NonAiAgentInstance;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.planner.AgentArgument;
import dev.langchain4j.agentic.planner.AgenticService;
import dev.langchain4j.agentic.supervisor.SupervisorAgentService;
import dev.langchain4j.agentic.workflow.LoopAgentService;
import dev.langchain4j.cdi.aiservice.CdiLookupHelper;
import dev.langchain4j.cdi.spi.RegisterAgent;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility to build LangChain4j agent proxies from CDI beans and the {@link RegisterAgent} metadata.
 *
 * <p>Mirrors {@link dev.langchain4j.cdi.aiservice.CommonAIServiceCreator} but creates agentic systems instead of plain
 * AI services. The method {@link #create(Instance, Class)} inspects the interface for {@link RegisterAgent}, resolves
 * CDI beans by name, and delegates to the appropriate {@link AgenticServices} builder.
 */
public class CommonAgentCreator {

    private static final Logger LOGGER = Logger.getLogger(CommonAgentCreator.class.getName());

    /** CDI bean name prefix for agents that have no explicit name set in {@link RegisterAgent#name()}. */
    public static final String AGENT_BEAN_NAME_PREFIX = "registeredAgent-";

    private static final String AGENT_TOSTRING_PREFIX = "Agent[";

    public static <X> X create(Instance<Object> lookup, Class<X> interfaceClass) {
        RegisterAgent annotation = interfaceClass.getAnnotation(RegisterAgent.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    "Interface " + interfaceClass.getName() + " must be annotated with @RegisterAgent");
        }

        List<Object> subAgents = resolveSubAgents(lookup, annotation.subAgentNames());

        return switch (annotation.topology()) {
            case SIMPLE -> {
                ChatModel chatModel =
                        CdiLookupHelper.resolveSingle(lookup, ChatModel.class, annotation.chatModelName());
                StreamingChatModel streamingChatModel = CdiLookupHelper.resolveSingle(
                        lookup, StreamingChatModel.class, annotation.streamingChatModelName());
                yield buildSimple(interfaceClass, annotation, chatModel, streamingChatModel, lookup);
            }
            case SEQUENCE ->
                buildComposed(AgenticServices.sequenceBuilder(interfaceClass), annotation, subAgents, lookup);
            case LOOP -> {
                LoopAgentService<X> loopBuilder = AgenticServices.loopBuilder(interfaceClass);
                loopBuilder.maxIterations(annotation.maxIterations());
                yield buildComposed(loopBuilder, annotation, subAgents, lookup);
            }
            case PARALLEL ->
                buildComposed(AgenticServices.parallelBuilder(interfaceClass), annotation, subAgents, lookup);
            case CONDITIONAL ->
                buildComposed(AgenticServices.conditionalBuilder(interfaceClass), annotation, subAgents, lookup);
            case SUPERVISOR -> {
                ChatModel chatModel =
                        CdiLookupHelper.resolveSingle(lookup, ChatModel.class, annotation.chatModelName());
                yield buildSupervisor(interfaceClass, annotation, chatModel, subAgents, lookup);
            }
            case PLANNER ->
                buildComposed(AgenticServices.plannerBuilder(interfaceClass), annotation, subAgents, lookup);
            case A2A -> loadA2ABuilder().build(interfaceClass, annotation, lookup);
        };
    }

    @SuppressWarnings("unchecked")
    private static <X> X buildSimple(
            Class<X> interfaceClass,
            RegisterAgent annotation,
            ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            Instance<Object> lookup) {
        Method entryMethod = findEntryMethod(interfaceClass);
        if (entryMethod != null && entryMethod.isAnnotationPresent(Agent.class)) {
            return AgenticServices.createAgenticSystem(interfaceClass, chatModel, ctx -> {
                var builder = ctx.agentBuilder();
                if (streamingChatModel != null) {
                    builder.streamingChatModel(streamingChatModel);
                }
                AgentComponents.resolve(annotation, lookup, interfaceClass.getSimpleName())
                        .applyTo(builder);
                applyListener(builder::listener, annotation, lookup);
            });
        }
        return buildSimpleFromAnnotation(
                interfaceClass, annotation, chatModel, streamingChatModel, entryMethod, lookup);
    }

    @SuppressWarnings("unchecked")
    private static <X> X buildSimpleFromAnnotation(
            Class<X> interfaceClass,
            RegisterAgent annotation,
            ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            Method entryMethod,
            Instance<Object> lookup) {
        X aiService = buildAiService(interfaceClass, annotation, chatModel, streamingChatModel, lookup);
        String name = CdiLookupHelper.resolveExpression(annotation.name());
        if (!hasText(name)) {
            name = entryMethod != null ? entryMethod.getName() : interfaceClass.getSimpleName();
        }
        String description = CdiLookupHelper.resolveExpression(annotation.description());
        if (!hasText(description)) {
            description = "";
        }
        String outputKey = CdiLookupHelper.resolveExpression(annotation.outputKey());
        if (!hasText(outputKey)) {
            outputKey = "";
        }
        boolean async = annotation.async();

        List<AgentArgument> arguments = entryMethod != null ? AgentUtil.argumentsFromMethod(entryMethod) : List.of();
        AgentListener listener =
                CdiLookupHelper.resolveSingle(lookup, AgentListener.class, annotation.agentListenerName());

        NonAiAgentInstance agentInstance = new NonAiAgentInstance(
                interfaceClass,
                name,
                description,
                entryMethod != null ? entryMethod.getGenericReturnType() : String.class,
                outputKey,
                async,
                arguments,
                listener);

        InvocationHandler handler = (proxy, method, args) -> {
            Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> AGENT_TOSTRING_PREFIX + agentInstance.name() + "]";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> method.invoke(aiService, args);
                };
            }
            if (InternalAgent.class.isAssignableFrom(declaringClass)) {
                return method.invoke(agentInstance, args);
            }
            return method.invoke(aiService, args);
        };
        return AgentUtil.buildAgent(interfaceClass, handler);
    }

    private static <X> X buildAiService(
            Class<X> interfaceClass,
            RegisterAgent annotation,
            ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            Instance<Object> lookup) {
        AiServices<X> builder = AiServices.builder(interfaceClass);
        if (chatModel != null) {
            builder.chatModel(chatModel);
        }
        if (streamingChatModel != null) {
            builder.streamingChatModel(streamingChatModel);
        }
        AgentComponents.resolve(annotation, lookup, interfaceClass.getSimpleName())
                .applyTo(builder);
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static <X, S extends AgenticService<S, X>> X buildComposed(
            S builder, RegisterAgent annotation, List<Object> subAgents, Instance<Object> lookup) {
        configureAgenticService(builder, annotation, subAgents);
        applyListener(builder::listener, annotation, lookup);
        return builder.build();
    }

    private static <X> X buildSupervisor(
            Class<X> interfaceClass,
            RegisterAgent annotation,
            ChatModel chatModel,
            List<Object> subAgents,
            Instance<Object> lookup) {
        SupervisorAgentService<X> builder = AgenticServices.supervisorBuilder(interfaceClass);
        if (chatModel != null) {
            builder.chatModel(chatModel);
        }
        ChatMemoryProvider chatMemoryProvider =
                CdiLookupHelper.resolveSingle(lookup, ChatMemoryProvider.class, annotation.chatMemoryProviderName());
        if (chatMemoryProvider != null) {
            builder.chatMemoryProvider(chatMemoryProvider);
        }
        builder.maxAgentsInvocations(annotation.maxAgentsInvocations());
        configureCommonFields(
                builder::subAgents, builder::name, builder::description, builder::outputKey, annotation, subAgents);
        applyListener(builder::listener, annotation, lookup);
        return builder.build();
    }

    private static <S extends AgenticService<S, ?>> void configureAgenticService(
            S builder, RegisterAgent annotation, List<Object> subAgents) {
        configureCommonFields(
                builder::subAgents, builder::name, builder::description, builder::outputKey, annotation, subAgents);
    }

    private static void configureCommonFields(
            Consumer<Collection<?>> subAgentsSetter,
            Consumer<String> nameSetter,
            Consumer<String> descriptionSetter,
            Consumer<String> outputKeySetter,
            RegisterAgent annotation,
            List<Object> subAgents) {
        if (!subAgents.isEmpty()) {
            subAgentsSetter.accept(subAgents);
        }
        String name = CdiLookupHelper.resolveExpression(annotation.name());
        if (hasText(name)) {
            nameSetter.accept(name);
        }
        String description = CdiLookupHelper.resolveExpression(annotation.description());
        if (hasText(description)) {
            descriptionSetter.accept(description);
        }
        String outputKey = CdiLookupHelper.resolveExpression(annotation.outputKey());
        if (hasText(outputKey)) {
            outputKeySetter.accept(outputKey);
        }
    }

    private static void applyListener(
            Consumer<AgentListener> setter, RegisterAgent annotation, Instance<Object> lookup) {
        AgentListener listener =
                CdiLookupHelper.resolveSingle(lookup, AgentListener.class, annotation.agentListenerName());
        if (listener != null) {
            setter.accept(listener);
        }
    }

    /**
     * Resolved CDI beans for tools, memory, retrieval, and guardrails, shared between the {@link AiServices} and
     * {@link AgentBuilder} wiring paths.
     */
    private record AgentComponents(
            ToolProvider toolProvider,
            List<Object> tools,
            ChatMemory chatMemory,
            ChatMemoryProvider chatMemoryProvider,
            RetrievalAugmentor retrievalAugmentor,
            ContentRetriever contentRetriever,
            List<InputGuardrail> inputGuardrails,
            List<OutputGuardrail> outputGuardrails) {

        static AgentComponents resolve(RegisterAgent annotation, Instance<Object> lookup, String interfaceName) {
            ToolProvider toolProvider =
                    CdiLookupHelper.resolveSingle(lookup, ToolProvider.class, annotation.toolProviderName());
            List<Object> tools = toolProvider == null && annotation.tools().length > 0
                    ? CdiLookupHelper.resolveToolInstances(annotation.tools(), lookup)
                    : List.of();
            ChatMemory chatMemory =
                    CdiLookupHelper.resolveSingle(lookup, ChatMemory.class, annotation.chatMemoryName());
            ChatMemoryProvider chatMemoryProvider = CdiLookupHelper.resolveSingle(
                    lookup, ChatMemoryProvider.class, annotation.chatMemoryProviderName());
            RetrievalAugmentor retrievalAugmentor = CdiLookupHelper.resolveSingle(
                    lookup, RetrievalAugmentor.class, annotation.retrievalAugmentorName());
            ContentRetriever contentRetriever = retrievalAugmentor == null
                    ? CdiLookupHelper.resolveSingle(lookup, ContentRetriever.class, annotation.contentRetrieverName())
                    : null;
            List<InputGuardrail> inputGuardrails = CdiLookupHelper.resolveInputGuardrails(
                    lookup, annotation.inputGuardrails(), annotation.inputGuardrailNames(), interfaceName);
            List<OutputGuardrail> outputGuardrails = CdiLookupHelper.resolveOutputGuardrails(
                    lookup, annotation.outputGuardrails(), annotation.outputGuardrailNames(), interfaceName);
            return new AgentComponents(
                    toolProvider,
                    tools,
                    chatMemory,
                    chatMemoryProvider,
                    retrievalAugmentor,
                    contentRetriever,
                    inputGuardrails,
                    outputGuardrails);
        }

        void applyTo(AiServices<?> builder) {
            if (toolProvider != null) {
                builder.toolProvider(toolProvider);
            } else if (!tools.isEmpty()) {
                builder.tools(tools);
            }
            if (chatMemory != null) {
                builder.chatMemory(chatMemory);
            }
            if (chatMemoryProvider != null) {
                builder.chatMemoryProvider(chatMemoryProvider);
            }
            if (retrievalAugmentor != null) {
                builder.retrievalAugmentor(retrievalAugmentor);
            } else if (contentRetriever != null) {
                builder.contentRetriever(contentRetriever);
            }
            if (!inputGuardrails.isEmpty()) {
                builder.inputGuardrails(inputGuardrails);
            }
            if (!outputGuardrails.isEmpty()) {
                builder.outputGuardrails(outputGuardrails);
            }
        }

        @SuppressWarnings("unchecked")
        void applyTo(AgentBuilder<?, ?> builder) {
            if (toolProvider != null) {
                builder.toolProvider(toolProvider);
            } else if (!tools.isEmpty()) {
                builder.tools(tools.toArray(new Object[0]));
            }
            if (chatMemory != null) {
                builder.chatMemory(chatMemory);
            }
            if (chatMemoryProvider != null) {
                builder.chatMemoryProvider(chatMemoryProvider);
            }
            if (retrievalAugmentor != null) {
                builder.retrievalAugmentor(retrievalAugmentor);
            } else if (contentRetriever != null) {
                builder.contentRetriever(contentRetriever);
            }
            if (!inputGuardrails.isEmpty()) {
                builder.inputGuardrails(inputGuardrails.toArray(InputGuardrail[]::new));
            }
            if (!outputGuardrails.isEmpty()) {
                builder.outputGuardrails(outputGuardrails.toArray(OutputGuardrail[]::new));
            }
        }
    }

    private static List<Object> resolveSubAgents(Instance<Object> lookup, String[] subAgentNames) {
        List<Object> subAgents = new ArrayList<>();
        for (String name : subAgentNames) {
            name = CdiLookupHelper.resolveExpression(name);
            if (!hasText(name)) {
                continue;
            }
            Instance<Object> beanInstance = lookup.select(Object.class, NamedLiteral.of(name));
            if (beanInstance.isResolvable()) {
                subAgents.add(toAgentExecutor(beanInstance.get()));
            } else {
                LOGGER.log(Level.WARNING, "Sub-agent ''{0}'' is not resolvable, skipping", name);
            }
        }
        return subAgents;
    }

    /**
     * Wraps a CDI-managed agent bean into an {@link AgentExecutor} suitable for use with agentic builders. CDI client
     * proxies (e.g., Weld proxies for {@code @ApplicationScoped} beans) lose method annotations, which prevents
     * {@link AgentUtil#agentToExecutor(Object)} from finding the agent's entry point. This method inspects the original
     * interface instead.
     *
     * <p>Use this when passing CDI-injected agent beans directly to programmatic agentic builders:
     *
     * <pre>{@code
     * AgenticServices.loopBuilder()
     *     .subAgents(CommonAgentCreator.toAgentExecutor(scorer), editor)
     *     .build();
     * }</pre>
     */
    public static Object toAgentExecutor(Object bean) {
        if (bean instanceof AgentExecutor) {
            return bean;
        }
        if (bean instanceof InternalAgent ia) {
            for (Class<?> iface : bean.getClass().getInterfaces()) {
                if (iface.isAnnotationPresent(RegisterAgent.class)) {
                    Method entryMethod = findEntryMethod(iface);
                    if (entryMethod != null) {
                        RegisterAgent ann = iface.getAnnotation(RegisterAgent.class);
                        String rawName = CdiLookupHelper.resolveExpression(ann.name());
                        String name = hasText(rawName) ? rawName : entryMethod.getName();
                        String desc = CdiLookupHelper.resolveExpression(ann.description());
                        String outputKey = CdiLookupHelper.resolveExpression(ann.outputKey());
                        AgentInvoker invoker =
                                AgentUtil.nonAiAgentInvoker(entryMethod, name, desc, outputKey, ann.async());
                        return new AgentExecutor(invoker, ia);
                    }
                }
            }
            return AgentUtil.agentToExecutor(ia);
        }
        return AgentUtil.agentToExecutor(bean);
    }

    private static final class A2ABuilderHolder {
        static final dev.langchain4j.cdi.agent.spi.A2AAgentBuilder INSTANCE = ServiceLoader.load(
                        dev.langchain4j.cdi.agent.spi.A2AAgentBuilder.class)
                .findFirst()
                .orElse(null);
    }

    private static dev.langchain4j.cdi.agent.spi.A2AAgentBuilder loadA2ABuilder() {
        if (A2ABuilderHolder.INSTANCE == null) {
            throw new IllegalStateException(
                    "A2A topology requires the langchain4j-cdi-a2a dependency on the classpath. "
                            + "Add dev.langchain4j.cdi:langchain4j-cdi-a2a to your project.");
        }
        return A2ABuilderHolder.INSTANCE;
    }

    private static Method findEntryMethod(Class<?> agentInterface) {
        List<Method> abstractMethods = Arrays.stream(agentInterface.getDeclaredMethods())
                .filter(m -> !m.isDefault())
                .toList();
        if (abstractMethods.size() > 1) {
            throw new IllegalArgumentException("@RegisterAgent interface "
                    + agentInterface.getName()
                    + " must declare exactly one abstract method, but found: "
                    + abstractMethods.stream().map(Method::getName).collect(Collectors.joining(", ")));
        }
        return abstractMethods.isEmpty() ? null : abstractMethods.get(0);
    }
}

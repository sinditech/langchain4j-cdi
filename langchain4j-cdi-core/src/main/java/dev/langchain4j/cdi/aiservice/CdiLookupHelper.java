package dev.langchain4j.cdi.aiservice;

import dev.langchain4j.cdi.spi.ExpressionResolver;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrail;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shared CDI bean resolution utilities used by both {@link CommonAIServiceCreator} and
 * {@link dev.langchain4j.cdi.agent.CommonAgentCreator}.
 */
public final class CdiLookupHelper {

    private static final Logger LOGGER = Logger.getLogger(CdiLookupHelper.class.getName());
    private static final String DEFAULT_BEAN_NAME = "#default";
    private static final List<ExpressionResolver> EXPRESSION_RESOLVERS;

    static {
        List<ExpressionResolver> resolvers = new ArrayList<>();
        ServiceLoader.load(ExpressionResolver.class).forEach(resolvers::add);
        EXPRESSION_RESOLVERS = Collections.unmodifiableList(resolvers);
    }

    private CdiLookupHelper() {}

    /** Returns {@code true} when {@code s} is non-null and contains at least one non-whitespace character. */
    public static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    /**
     * Resolves any expression embedded in {@code value} by applying all {@link ExpressionResolver} implementations
     * discovered via {@link ServiceLoader}, in discovery order. Returns {@code value} unchanged when no resolvers are
     * registered or the value is blank/null.
     */
    public static String resolveExpression(String value) {
        if (!hasText(value)) {
            return value;
        }
        String resolved = value;
        for (ExpressionResolver resolver : EXPRESSION_RESOLVERS) {
            resolved = resolver.resolve(resolved);
        }
        return resolved;
    }

    /**
     * Resolve a CDI Instance for the given type and name. If name is {@code "#default"}, select the default bean of the
     * given type. If name is blank or null, returns null (meaning: do not attempt to resolve). Expression patterns in
     * {@code name} are expanded via {@link #resolveExpression(String)} before lookup.
     */
    public static <X> Instance<X> getInstance(Instance<Object> lookup, Class<X> type, String name) {
        String resolved = resolveExpression(name);
        if (hasText(resolved)) {
            if (DEFAULT_BEAN_NAME.equals(resolved)) {
                LOGGER.fine(() -> "Resolving default " + type.getSimpleName() + " bean");
                return lookup.select(type);
            }
            LOGGER.fine(() -> "Resolving " + type.getSimpleName() + " bean named '" + resolved + "'");
            return lookup.select(type, NamedLiteral.of(resolved));
        }
        return null;
    }

    /**
     * Resolve a single bean instance by type and name, returning null if unresolvable. Convenience wrapper around
     * {@link #getInstance} that extracts the bean.
     */
    public static <X> X resolveSingle(Instance<Object> lookup, Class<X> type, String name) {
        Instance<X> instance = getInstance(lookup, type, name);
        if (instance != null && instance.isResolvable()) {
            return instance.get();
        }
        return null;
    }

    /**
     * Resolve guardrail instances by class. For each class, first attempts CDI lookup; if the bean is not resolvable,
     * falls back to instantiation via the no-arg constructor. Classes that fail both resolution paths are skipped with
     * a WARNING log.
     */
    public static <G> List<G> resolveGuardrailsByClass(Instance<Object> lookup, Class<? extends G>[] guardrailClasses) {
        List<G> guardrails = new ArrayList<>(guardrailClasses.length);
        for (Class<? extends G> guardrailClass : guardrailClasses) {
            try {
                Instance<? extends G> guardrailInstance = lookup.select(guardrailClass);
                if (guardrailInstance != null && guardrailInstance.isResolvable()) {
                    guardrails.add(guardrailInstance.get());
                } else {
                    guardrails.add(
                            guardrailClass.getConstructor((Class<?>[]) null).newInstance((Object[]) null));
                }
            } catch (ReflectiveOperationException | IllegalArgumentException ex) {
                LOGGER.log(
                        Level.WARNING,
                        "Failed to create guardrail " + guardrailClass + ", skipping: " + ex.getMessage(),
                        ex);
            }
        }
        return guardrails;
    }

    /**
     * Resolve guardrail instances by named CDI bean lookup. Names that cannot be resolved are skipped with a WARNING
     * log.
     */
    public static <G> List<G> resolveGuardrailsByName(Instance<Object> lookup, Class<G> type, String[] names) {
        List<G> guardrails = new ArrayList<>(names.length);
        for (String name : names) {
            try {
                Instance<G> guardrailInstance = getInstance(lookup, type, name);
                if (guardrailInstance != null && guardrailInstance.isResolvable()) {
                    guardrails.add(guardrailInstance.get());
                } else {
                    LOGGER.log(Level.WARNING, "Named guardrail ''{0}'' is not resolvable, skipping", name);
                }
            } catch (IllegalArgumentException ex) {
                LOGGER.log(
                        Level.WARNING, "Failed to resolve guardrail '" + name + "', skipping: " + ex.getMessage(), ex);
            }
        }
        return guardrails;
    }

    /**
     * Resolve tool instances by named CDI bean lookup. Names that cannot be resolved are skipped with a WARNING log.
     */
    public static List<Object> resolveToolsByName(String[] toolNames, Instance<Object> lookup) {
        List<Object> tools = new ArrayList<>(toolNames.length);
        for (String name : toolNames) {
            String resolved = resolveExpression(name);
            if (!hasText(resolved)) {
                continue;
            }
            Instance<Object> instance = lookup.select(Object.class, NamedLiteral.of(resolved));
            if (instance.isResolvable()) {
                tools.add(instance.get());
            } else {
                LOGGER.log(Level.WARNING, "Named tool ''{0}'' is not resolvable, skipping", resolved);
            }
        }
        return tools;
    }

    /**
     * Resolve tool instances from an array of tool classes. For each class, first attempts CDI lookup; if the bean is
     * not resolvable, falls back to instantiation via the no-arg constructor. Classes that fail both resolution paths
     * are skipped with a SEVERE log.
     */
    public static List<Object> resolveToolInstances(Class<?>[] toolClasses, Instance<Object> lookup) {
        List<Object> tools = new ArrayList<>(toolClasses.length);
        for (Class<?> toolClass : toolClasses) {
            try {
                Instance<?> toolInstance = lookup.select(toolClass);
                if (toolInstance != null && toolInstance.isResolvable()) {
                    tools.add(toolInstance.get());
                } else {
                    tools.add(toolClass.getConstructor((Class<?>[]) null).newInstance((Object[]) null));
                }
            } catch (ReflectiveOperationException | IllegalArgumentException ex) {
                LOGGER.log(Level.SEVERE, "Failed to create tool " + toolClass + ", skipping: " + ex.getMessage(), ex);
            }
        }
        return tools;
    }

    /**
     * Resolve input guardrails using class-vs-name precedence. When both {@code guardrailClasses} and
     * {@code guardrailNames} are non-empty, classes take precedence and a WARNING is logged including
     * {@code interfaceName} so operators can identify the offending service.
     */
    public static List<InputGuardrail> resolveInputGuardrails(
            Instance<Object> lookup,
            Class<? extends InputGuardrail>[] guardrailClasses,
            String[] guardrailNames,
            String interfaceName) {
        if (guardrailClasses.length > 0 && guardrailNames.length > 0) {
            LOGGER.log(
                    Level.WARNING,
                    "Both inputGuardrails and inputGuardrailNames specified for {0}. Using inputGuardrails classes and ignoring inputGuardrailNames.",
                    interfaceName);
        }
        if (guardrailClasses.length > 0) {
            return resolveGuardrailsByClass(lookup, guardrailClasses);
        } else if (guardrailNames.length > 0) {
            return resolveGuardrailsByName(lookup, InputGuardrail.class, guardrailNames);
        }
        return List.of();
    }

    /**
     * Resolve output guardrails using class-vs-name precedence. When both {@code guardrailClasses} and
     * {@code guardrailNames} are non-empty, classes take precedence and a WARNING is logged including
     * {@code interfaceName} so operators can identify the offending service.
     */
    public static List<OutputGuardrail> resolveOutputGuardrails(
            Instance<Object> lookup,
            Class<? extends OutputGuardrail>[] guardrailClasses,
            String[] guardrailNames,
            String interfaceName) {
        if (guardrailClasses.length > 0 && guardrailNames.length > 0) {
            LOGGER.log(
                    Level.WARNING,
                    "Both outputGuardrails and outputGuardrailNames specified for {0}. Using outputGuardrails classes and ignoring outputGuardrailNames.",
                    interfaceName);
        }
        if (guardrailClasses.length > 0) {
            return resolveGuardrailsByClass(lookup, guardrailClasses);
        } else if (guardrailNames.length > 0) {
            return resolveGuardrailsByName(lookup, OutputGuardrail.class, guardrailNames);
        }
        return List.of();
    }
}

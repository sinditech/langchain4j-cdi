package dev.langchain4j.cdi.decision;

import java.util.logging.Logger;

import dev.langchain4j.cdi.aiservice.CdiLookupHelper;
import dev.langchain4j.cdi.spi.RegisterDecisionService;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.service.decision.DecisionServices;
import dev.langchain4j.service.decision.ThresholdProvider;
import jakarta.enterprise.inject.Instance;

/**
 * Utility to build LangChain4j DecisionServices proxies from CDI beans and the @RegisterDecisionService metadata.
 *
 * <p>The method create() inspects the provided service interface for @RegisterDecisionService and tries to resolve optional
 * collaborating beans from the CDI container (by name or default).
 *
 * <p>Only the components that are resolvable are wired into the DecisionServices builder.
 */
public class CommonDecisionServiceCreator {

    /** Utility class — not instantiable. */
    private CommonDecisionServiceCreator() {}

    private static final Logger LOGGER = Logger.getLogger(CommonDecisionServiceCreator.class.getName());

    /**
     * Create a LangChain4j Decision service proxy for the given annotated interface.
     *
     * @param <X> the AI service interface type
     * @param lookup CDI Instance used to resolve named beans (models, tools, memories, etc.).
     * @param interfaceClass the AI service interface annotated with {@link RegisterDecisionService}.
     * @return a runtime proxy implementing the given interface.
     */
    public static <X> X create(Instance<Object> lookup, Class<X> interfaceClass) {
    	RegisterDecisionService annotation = interfaceClass.getAnnotation(RegisterDecisionService.class);
        if (annotation == null) {
            throw new IllegalArgumentException(
                    "Interface " + interfaceClass.getName() + " must be annotated with @RegisterDecisionService");
        }
        String decisionModelName = annotation.decisionModelName();
        // Instances
        Instance<DecisionModel> decisionModelInstance = CdiLookupHelper.getInstance(lookup, DecisionModel.class, decisionModelName);
        Instance<ThresholdProvider> thresholdProvider =
                CdiLookupHelper.getInstance(lookup, ThresholdProvider.class, annotation.thresholdProviderName());

        DecisionServices.Builder<X> builder = DecisionServices.builder(interfaceClass);
        if (decisionModelInstance != null && decisionModelInstance.isResolvable()) {
            LOGGER.fine("DecisionModel " + decisionModelInstance.get());
            builder.decisionModel(decisionModelInstance.get());
        }
        if (thresholdProvider != null && thresholdProvider.isResolvable()) {
            LOGGER.fine("ThresholdProvider " + thresholdProvider.get());
            builder.thresholdProvider(thresholdProvider.get());
        }
        return builder.build();
    }
}

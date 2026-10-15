package dev.langchain4j.cdi.core.buildcompatibleextension;

import dev.langchain4j.cdi.decision.CommonDecisionServiceCreator;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;

/**
 * Synthetic bean creator that instantiates AI decision service proxies at runtime using the interface class stored
 * during build-time synthesis.
 */
public class DecisionServiceCreator implements SyntheticBeanCreator<Object> {

    /** Creates a new {@code DecisionServiceCreator}. */
    public DecisionServiceCreator() {}

    @Override
    public Object create(Instance<Object> lookup, Parameters params) {
        return CommonDecisionServiceCreator.create(
                lookup, params.get(Langchain4JAIServiceBuildCompatibleExtension.PARAM_INTERFACE_CLASS, Class.class));
    }
}

package dev.langchain4j.cdi.agent;

import static org.mockito.Mockito.mock;

import dev.langchain4j.agentic.declarative.PlannerSupplier;
import dev.langchain4j.agentic.planner.Planner;
import dev.langchain4j.cdi.spi.RegisterAgent;
import dev.langchain4j.service.V;

/**
 * Public top-level interface required for the PLANNER topology test. {@code DeclarativeUtil.invokeStatic} calls
 * {@code method.invoke(null)} on the {@code @PlannerSupplier} method; this requires the declaring class to be a public
 * type accessible from the {@code langchain4j-agentic} named module.
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RegisterAgent(
        topology = AgentTopologyType.PLANNER,
        subAgentNames = {"plannerWorker"})
public interface TestPlannerOrchestrator {

    String process(@V("input") String input);

    @PlannerSupplier
    static Planner providePlanner() {
        return mock(Planner.class);
    }
}

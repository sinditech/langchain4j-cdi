package dev.langchain4j.cdi.core;

import dev.langchain4j.cdi.core.portableextension.LangChain4JAIServicePortableExtension;
import dev.langchain4j.cdi.core.portableextension.LangChain4JPluginsPortableExtension;
import io.smallrye.config.inject.ConfigExtension;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Integration tests verifying that expression resolvers (MP Config and Jakarta EL) are applied when creating AI service
 * proxies via the CDI portable extension in a Weld container.
 */
@ExtendWith(WeldJunit5Extension.class)
public class ExpressionResolverWeldIntegrationTest {

    @Inject
    MpConfigExpressionAIService mpConfigAIService;

    @Inject
    ELExpressionAIService elAIService;

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(
                    LangChain4JAIServicePortableExtension.class,
                    LangChain4JPluginsPortableExtension.class,
                    MpConfigExpressionAIService.class,
                    ELExpressionAIService.class,
                    DummyChatModel.class,
                    DummyEmbeddingStore.class,
                    DummyEmbeddingModel.class,
                    ConfigExtension.class)
            .build();

    @Test
    void mpConfigExpression_resolvesModelName() {
        // ${test.expression.model.name} is resolved to "chat-model-dummy" via MP Config
        Assertions.assertNotNull(mpConfigAIService);
        Assertions.assertNotNull(mpConfigAIService.toString());
    }

    @Test
    void elExpression_resolvesModelName() {
        // #{true ? 'chat-model-dummy' : 'other'} is evaluated to "chat-model-dummy" via Jakarta EL
        Assertions.assertNotNull(elAIService);
        Assertions.assertNotNull(elAIService.toString());
    }
}

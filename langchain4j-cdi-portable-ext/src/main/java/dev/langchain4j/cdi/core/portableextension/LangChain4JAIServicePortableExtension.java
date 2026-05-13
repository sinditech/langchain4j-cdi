package dev.langchain4j.cdi.core.portableextension;

import dev.langchain4j.cdi.spi.RegisterAIService;
import dev.langchain4j.cdi.spi.RegisterAgent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.enterprise.inject.spi.WithAnnotations;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class LangChain4JAIServicePortableExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger(LangChain4JAIServicePortableExtension.class.getName());
    private static final Set<Class<?>> detectedAIServicesDeclaredInterfaces = new HashSet<>();
    private static final Set<Class<?>> detectedAgentDeclaredInterfaces = new HashSet<>();

    public static Set<Class<?>> getDetectedAIServicesDeclaredInterfaces() {
        return detectedAIServicesDeclaredInterfaces;
    }

    public static Set<Class<?>> getDetectedAgentDeclaredInterfaces() {
        return detectedAgentDeclaredInterfaces;
    }

    // These sets are static so they persist across container restarts in the same JVM (e.g. Arquillian,
    // Weld SE restart). The BeforeBeanDiscovery observer below clears them at the start of each new
    // container lifecycle to prevent stale entries from a previous run from leaking into the new one.
    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
        detectedAIServicesDeclaredInterfaces.clear();
        detectedAgentDeclaredInterfaces.clear();
    }

    <T> void processAnnotatedType(@Observes @WithAnnotations({RegisterAIService.class}) ProcessAnnotatedType<T> pat) {
        if (pat.getAnnotatedType().getJavaClass().isInterface()) {
            LOGGER.info("processAnnotatedType register "
                    + pat.getAnnotatedType().getJavaClass().getName());
            detectedAIServicesDeclaredInterfaces.add(pat.getAnnotatedType().getJavaClass());
        } else {
            LOGGER.warning("processAnnotatedType reject "
                    + pat.getAnnotatedType().getJavaClass().getName() + " which is not an interface");
            pat.veto();
        }
    }

    <T> void processAgentAnnotatedType(@Observes @WithAnnotations({RegisterAgent.class}) ProcessAnnotatedType<T> pat) {
        if (pat.getAnnotatedType().getJavaClass().isInterface()) {
            LOGGER.info("processAnnotatedType register agent "
                    + pat.getAnnotatedType().getJavaClass().getName());
            detectedAgentDeclaredInterfaces.add(pat.getAnnotatedType().getJavaClass());
        } else {
            LOGGER.warning("processAnnotatedType reject "
                    + pat.getAnnotatedType().getJavaClass().getName() + " which is not an interface");
            pat.veto();
        }
    }

    /**
     * This is useful for application servers that can't support proccessAnnotatedType.
     *
     * @param event
     */
    void processInjectionPoints(@Observes ProcessInjectionPoint<?, ?> event) {
        if (event.getInjectionPoint().getBean() == null) {
            Class<?> rawType = Reflections.getRawType(event.getInjectionPoint().getType());
            if (classSatisfies(rawType, RegisterAIService.class)) detectedAIServicesDeclaredInterfaces.add(rawType);
            if (classSatisfies(rawType, RegisterAgent.class)) detectedAgentDeclaredInterfaces.add(rawType);
        }

        if (Instance.class.equals(
                Reflections.getRawType(event.getInjectionPoint().getType()))) {
            Class<?> parameterizedType = Reflections.getRawType(getFacadeType(event.getInjectionPoint()));
            if (classSatisfies(parameterizedType, RegisterAIService.class))
                detectedAIServicesDeclaredInterfaces.add(parameterizedType);
            if (classSatisfies(parameterizedType, RegisterAgent.class))
                detectedAgentDeclaredInterfaces.add(parameterizedType);
        }
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager)
            throws ClassNotFoundException {
        for (Class<?> aiServiceClass : detectedAIServicesDeclaredInterfaces) {
            LOGGER.info("afterBeanDiscovery create synthetic AI service:  " + aiServiceClass.getName());
            afterBeanDiscovery.addBean(new LangChain4JAIServiceBean<>(aiServiceClass, beanManager));
        }
        for (Class<?> agentClass : detectedAgentDeclaredInterfaces) {
            LOGGER.info("afterBeanDiscovery create synthetic agent:  " + agentClass.getName());
            afterBeanDiscovery.addBean(new LangChain4JAIAgentBean<>(agentClass, beanManager));
        }
    }

    private <T extends Annotation> boolean classSatisfies(Class<?> clazz, Class<T> annotationClass) {
        if (!clazz.isInterface()) return false;
        T annotation = clazz.getAnnotation(annotationClass);
        return (annotation != null);
    }

    private Type getFacadeType(InjectionPoint injectionPoint) {
        Type genericType = injectionPoint.getType();
        if (genericType instanceof ParameterizedType) {
            return ((ParameterizedType) genericType).getActualTypeArguments()[0];
        }
        return null;
    }
}

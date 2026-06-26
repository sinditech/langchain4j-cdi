/** */
package dev.langchain4j.cdi.faulttolerance.spi;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Intercepted;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Buhake Sindi
 * @since 24 July 2025
 */
@Interceptor
@ApplyFaultTolerance
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 100)
class ApplyFaultToleranceInterceptor {

    @Inject
    private BeanManager beanManager;

    @Inject
    @Intercepted
    private Bean<?> interceptedBean;

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Throwable {

        boolean isAiServiceBean = Langchain4JFaultToleranceExtension.AI_STEREOTYPES_ANNOTATIONS.stream()
                .anyMatch(interceptedBean.getBeanClass()::isAnnotationPresent);
        if (isAiServiceBean) {
            final InterceptionType interception = InterceptionType.AROUND_INVOKE;
            List<Annotation> annotations = Langchain4JFaultToleranceExtension.FAULT_TOLERANCE_ANNOTATIONS.stream()
                    .map(annotationClass -> invocationContext.getMethod().getAnnotation(annotationClass))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            List<jakarta.enterprise.inject.spi.Interceptor<?>> interceptors = beanManager.resolveInterceptors(
                    interception, annotations.toArray(new Annotation[annotations.size()]));

            if (!interceptors.isEmpty()) {
                return new FaultToleranceInterceptorHandler(beanManager, interception, interceptors)
                        .handle(invocationContext);
            }
        }

        return invocationContext.proceed();
    }
}

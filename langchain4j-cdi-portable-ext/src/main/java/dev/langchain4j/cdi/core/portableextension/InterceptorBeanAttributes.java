/** */
package dev.langchain4j.cdi.core.portableextension;

import jakarta.enterprise.inject.spi.BeanAttributes;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author Buhake Sindi
 * @since 26 June 2026
 */
public interface InterceptorBeanAttributes<T> extends BeanAttributes<T> {

    /**
     * Obtains the {@linkplain jakarta.interceptor.InterceptorBinding interceptorBindings} of the bean.
     *
     * @return the {@linkplain jakarta.interceptor.InterceptorBinding interceptorBindings}
     */
    public Set<Annotation> getInterceptorBindings();
}

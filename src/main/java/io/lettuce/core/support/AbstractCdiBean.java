package io.lettuce.core.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;

import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.ClientResources;

/**
 * @author Mark Paluch
 * @since 3.0
 */
abstract class AbstractCdiBean<T> implements Bean<T> {

    protected final Bean<RedisURI> redisURIBean;

    protected final Bean<ClientResources> clientResourcesBean;

    protected final BeanManager beanManager;

    protected final Set<Annotation> qualifiers;

    protected final String name;

    public AbstractCdiBean(Bean<RedisURI> redisURIBean, Bean<ClientResources> clientResourcesBean, BeanManager beanManager,
            Set<Annotation> qualifiers, String name) {
        this.redisURIBean = redisURIBean;
        this.clientResourcesBean = clientResourcesBean;
        this.beanManager = beanManager;
        this.qualifiers = qualifiers;
        this.name = name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Type> getTypes() {
        return Collections.singleton(getBeanClass());
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        Set<Class<? extends Annotation>> stereotypes = new HashSet<>();

        for (Annotation annotation : getQualifiers()) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType.isAnnotationPresent(Stereotype.class)) {
                stereotypes.add(annotationType);
            }
        }

        return stereotypes;
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

}

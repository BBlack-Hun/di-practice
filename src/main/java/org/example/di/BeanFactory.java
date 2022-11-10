package org.example.di;

import org.example.annotation.Inject;
import org.example.controller.UserController;
import org.reflections.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.sun.beans.finder.ConstructorFinder.findConstructor;

public class BeanFactory {

    private final Set<Class<?>> preInstantiatedClazz;

    private Map<Class<?>, Object> beans = new HashMap<>();

    public BeanFactory(Set<Class<?>> preInstantiatedClazz) {
        this.preInstantiatedClazz = preInstantiatedClazz;
        initialize();
    }

    private void initialize() {
        for (Class<?> clazz : preInstantiatedClazz) {
            Object instance = createdInstance(clazz);
            beans.put(clazz, instance);
        }
    }

    // UserController
    // UserService
    private Object createdInstance(Class<?> clazz) {
        // 생성자
        Constructor<?> constructor = findConstructor(clazz);

        // 파라미터
        List<Object> parameters = new ArrayList<>();
        for(Class<?> typeClass  : constructor.getParameterTypes()) {
            // UserService
            parameters.add(getParameterByClass(typeClass));
        }

        // 인스턴스 생성
        try {
            return constructor.newInstance(parameters.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Constructor<?> findConstructor(Class<?> clazz) {
        Constructor<?> constructor = BeanFactoryUtils.getInjectedConstructor(clazz);
        if (Objects.nonNull(constructor)) {
            return constructor;
        }
        return clazz.getConstructors()[0];
    }

    private Object getParameterByClass(Class<?> typeClass) {
        Object instanceBean = getBean(typeClass);
        if (Objects.nonNull(instanceBean)) {
            return instanceBean;
        }
        // 생성자가 없다면, 재귀 호출을 이용한 인스턴스 생성
        return createdInstance(typeClass);
    }

    public <T> T getBean(Class<T> requiredType) {
        return (T) beans.get(requiredType);
    }
}

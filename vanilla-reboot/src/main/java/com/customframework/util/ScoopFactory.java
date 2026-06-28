package com.customframework.util;


import com.customframework.annotation.Component;
import com.customframework.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;

public class ScoopFactory {
    private static final Logger log = LoggerFactory.getLogger(ScoopFactory.class);

    private final Map<Class<?>, Object> context = new HashMap<>();
    private final List<Class<?>> complexContext = new ArrayList<>();



    public Map<Class<?>, Object> instantiate(List<Class<?>> componentClasses) {

        try {
            for (Class<?> clazz : componentClasses) {
                if (clazz.isAnnotationPresent(Component.class)) {
                    boolean isComplex = false;
                    for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                        if (constructor.isAnnotationPresent(Inject.class)) {
                            isComplex = true;
                            complexContext.add(clazz);
                            break;
                        }
                    }

                    if (!isComplex) {
                        context.put(clazz, clazz.getDeclaredConstructor().newInstance());
                    }
                }
            }

            boolean iterationSwitch = true;

            while (iterationSwitch) {
                Iterator<Class<?>> iterator = complexContext.iterator();
                int complexSize = complexContext.size();
                while (iterator.hasNext()) {
                    Class<?> clazz = iterator.next();
                    List<Object> objects = new ArrayList<>();
                    boolean ready = true;

                    Constructor<?> targetConstructor = null;
                    for (Constructor<?> con : clazz.getDeclaredConstructors()) {
                        if (con.isAnnotationPresent(Inject.class)) {
                            targetConstructor = con;
                            break; // Нашли — сразу выходим из этого микро-поиска
                        }
                    }

                    if (targetConstructor == null) {
                        continue;
                    }
                    // 3. Работаем строго с параметрами ЭТОГО конструктора (без лишних циклов!)
                    Class<?>[] parameterTypes = targetConstructor.getParameterTypes();

                    for (Class<?> parameterType : parameterTypes) {
                        Object object = context.get(parameterType);
                        if (object != null) {
                            objects.add(object);
                            continue;
                        }
                        ready = false;
                        break;
                    }

                    if (ready) {
                        context.put(clazz, targetConstructor.newInstance(objects.toArray()));
                        iterator.remove();
                    }
                }

                if (complexSize == complexContext.size()) {
                    throw new RuntimeException("Circular dependency detected or missing dependency!");
                } else if (complexContext.isEmpty()) {
                    iterationSwitch = false;
                }
            }
            return context;

        } catch (Exception e) {
            log.error("Scoop instantiation failed", e);
            throw new RuntimeException(e);
        }
    }


}

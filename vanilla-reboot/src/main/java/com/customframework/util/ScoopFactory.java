package com.customframework.util;


import com.customframework.annotation.Component;
import com.customframework.annotation.Inject;
import com.customframework.annotation.PostConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ScoopFactory {
    private static final Logger log = LoggerFactory.getLogger(ScoopFactory.class);

    private final Map<Class<?>, Object> context = new HashMap<>();
    private final List<Class<?>> complexContext = new ArrayList<>();



    public Map<Class<?>, Object> instantiate(List<Class<?>> componentClasses) {

        try {
            for (Class<?> clazz : componentClasses) {
                if (clazz.isAnnotationPresent(Component.class)) {
                    boolean isComplex = isItComplex(clazz);

                    if(isComplex) {
                        complexContext.add(clazz);
                    }
                    else {
                        Object object = clazz.getDeclaredConstructor().newInstance();
                        context.put(clazz, object);
                        invokePostConstructor(object);
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

                    Constructor<?> targetConstructor = getTargetConstructor(clazz);

                    if (targetConstructor == null) {
                        continue;
                    }

                    // 3. Работаем строго с параметрами ЭТОГО конструктора (без лишних циклов!)
                    Class<?>[] parameterTypes = targetConstructor.getParameterTypes();

                    int index = -1;

                    for (Class<?> parameterType : parameterTypes) {

                        index++;

                        if(parameterType == List.class) {

                            Type gen = targetConstructor.getGenericParameterTypes()[index];
                            ParameterizedType pt = (ParameterizedType) gen;
                            Class<?> typeArgument = (Class<?>) pt.getActualTypeArguments()[0];

                            List<Object> list = fillDataStructure(typeArgument);

                            if(list == null) {
                                ready = false;
                                break;
                            }

                            objects.add(list);
                            continue;
                        }

                        Object object = context.get(parameterType);

                        if (object == null) {
                            for(Object createScoop : context.values()) {
                                if(parameterType.isAssignableFrom(createScoop.getClass())) {
                                    object = createScoop;
                                    System.out.println("🍦 [ПОЛИМОРФИЗМ] Для интерфейса " + parameterType.getSimpleName()
                                            + " нашли реализацию: " + object.getClass().getSimpleName());
                                    break;
                                }
                            }
                        }

                        if (object != null) {
                            objects.add(object);
                        }
                        else {
                            ready = false;
                            break;
                        }
                    }

                    if (ready) {
                        Object object = targetConstructor.newInstance(objects.toArray());
                        context.put(clazz,object);
                        System.out.println("🍦 Успешно создали scoop: " + clazz.getSimpleName());// <-- ДОБ
                        invokePostConstructor(object);
                        iterator.remove();
                    }
                }

                if (complexSize == complexContext.size()) {
                    throw new RuntimeException("Circular dependency detected or missing dependency!");
                } else if (complexContext.isEmpty()) {
                    iterationSwitch = false;
                }
            }

//            for(Object obj : context.values()) {
//                invokePostConstructor(obj);
//            }


            return context;

        } catch (Exception e) {
            log.error("Scoop instantiation failed", e);
            throw new RuntimeException(e);
        }
    }





    private static boolean isItComplex(Class<?> clazz) {
        boolean isComplex = false;
        Constructor<?>[] constructors = clazz.getConstructors();

        if(constructors.length == 1 && constructors[0].getParameterCount() > 0) {
            isComplex = true;
        }
        else {
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(Inject.class)) {
                    isComplex = true;
                    break;
                }
            }
        }
        return isComplex;
    }



    private static Constructor<?> getTargetConstructor(Class<?> clazz) {
        Constructor<?> targetConstructor = null;
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        if(constructors.length == 1 && constructors[0].getParameterTypes().length > 0) {
            targetConstructor = constructors[0];
        }
        else {
            for (Constructor<?> con : constructors) {
                if (con.isAnnotationPresent(Inject.class)) {
                    targetConstructor = con;
                    break; // Нашли — сразу выходим из этого микро-поиска
                }
            }
        }
        return targetConstructor;
    }


    private List<Object> fillDataStructure(Class<?> targetClass) {
        for(Class<?> complex : complexContext) {
            if(targetClass.isAssignableFrom(complex)) {
                return null;
            }
        }

        List<Object> classes = new ArrayList<>();

        Object object = context.get(targetClass);

        if(object == null) {
            for(Object clazz : context.values()) {
                if(targetClass.isAssignableFrom(clazz.getClass())) {
                    classes.add(clazz);
                }
            }
        }

        if(object != null) {
            classes.add(object);
        }
        return classes;

    }

    private void invokePostConstructor (Object scoop) {
        Class<?> targetClass = scoop.getClass();

        for(Method method : targetClass.getDeclaredMethods()) {
            if(method.isAnnotationPresent(PostConstructor.class)) {
                if(method.getParameterTypes().length > 0 ) {
                   throw new IllegalStateException("Метод @PostConstruct в классе " +
                            targetClass.getName() + " не должен иметь параметров!");
                }

                if(method.getReturnType() != void.class) {
                    throw new IllegalStateException("Метод @PostConstruct в классе " +
                            targetClass.getName() + " не должен быть возвращающим!");
                }

                try {
                    method.setAccessible(true);
                    method.invoke(scoop);
                } catch (Exception e) {
                    throw new RuntimeException("Не удалось вызвать @PostConstruct для бина "
                            + targetClass.getName(), e);
                }
            }
        }
    }


}

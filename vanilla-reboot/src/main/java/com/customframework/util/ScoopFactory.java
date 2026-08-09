package com.customframework.util;


import com.customframework.Scope;
import com.customframework.annotation.Component;
import com.customframework.annotation.Inject;
import com.customframework.annotation.PostConstructor;
import com.customframework.annotation.ScoopScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class ScoopFactory {
    private static final Logger log = LoggerFactory.getLogger(ScoopFactory.class);

    private final Map<Class<?>, Object> context = new HashMap<>();
    private final List<Class<?>> complexContext = new ArrayList<>();
    private final List<Class<?>> prototypes = new ArrayList<>();


    public List<Class<?>> getPrototypes() {
        return prototypes;
    }




    public Map<Class<?>, Object> instantiate(List<Class<?>> componentClasses) {

        try {
            for (Class<?> clazz : componentClasses) {
                if (isScoop(clazz)) {

                    if(checkScoopScope(clazz)) {
                        continue;
                    }

                    boolean isComplex = isItComplex(clazz); // sorting classes ( simple bean and complex bean )

                    if(isComplex) {
                        System.out.println("scoop " + clazz.getSimpleName() + " was added to complex context");
                        complexContext.add(clazz);
                    }
                    else {
                        Object object = clazz.getDeclaredConstructor().newInstance();
                        System.out.println("scoop " + clazz.getSimpleName() + " was added to context");
                        context.put(clazz, object);
                        invokePostConstructor(object);
                    }
                }
            }

            boolean iterationSwitch = true;


            while (iterationSwitch) { // first iteration, we need this in case we can't find all needed dependencies in one lap
                                      // to find all dependencies we need to iterate over and over until we find them all

                Iterator<Class<?>> iterator = complexContext.iterator(); // we need to remove a complex object from list if all its dependencies are founded
                int complexSize = complexContext.size();
                while (iterator.hasNext()) { // here's where we start our actual lap

                    Class<?> clazz = iterator.next();
                    List<Object> objects = new ArrayList<>(); // new list so we can store found dependencies for a complex object
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

                        Object object;

                        object = createOrGetScoop(parameterType);

//                        if (object == null) {
//                            for(Object createScoop : context.values()) {
//                                if(parameterType.isAssignableFrom(createScoop.getClass())) {
//                                    object = createScoop;
//                                    System.out.println("🍦 [ПОЛИМОРФИЗМ] Для интерфейса " + parameterType.getSimpleName()
//                                            + " нашли реализацию: " + object.getClass().getSimpleName());
//                                    break;
//                                }
//                            }
//                        }

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

            System.out.println("this is our prototypes size : " + prototypes.size());

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


    private boolean checkScoopScope(Class<?> clazz) {
        System.out.println("this is clazz: " + clazz.getSimpleName());
        ScoopScope annotation = clazz.getAnnotation(ScoopScope.class);

        if (annotation != null && annotation.value() == Scope.PROTOTYPE) {
            prototypes.add(clazz);
            System.out.println("prototypes: " + clazz.getSimpleName());
            return true;
        }



        return false;
    }


    public Object createOrGetScoop(Class<?> clazz) {
      try {


          List<Object> depends = new ArrayList<>();
          Object object = context.get(clazz);

          if (object != null) {
              System.out.println("⚠️ [DEBUG] " + clazz.getSimpleName() + " найден в context (СИНГЛТОН)!");
              return object;
          }

          System.out.println("we are in the middle of something");

          if (prototypes.contains(clazz)) {
              System.out.println("⚡ [DEBUG] " + clazz.getSimpleName() + " создается как ПРОТОТИП!");

              if (isItComplex(clazz)) {

                  Constructor<?> constructor = getTargetConstructor(clazz);
                  if (constructor != null) {
                      for (Class<?> parameterType : constructor.getParameterTypes()) {
                          Object depend = createOrGetScoop(parameterType);

                          if (depend != null) {
                              depends.add(depend);
                          } else {
                              throw new IllegalStateException
                                      ("a dependency called " + parameterType.getSimpleName() + " is not found");
                          }
                      }
                      Object complexObject =  constructor.newInstance(depends.toArray());
                      invokePostConstructor(complexObject);
                      return complexObject;

                  }

              } else {
                  Object newInstance = clazz.getDeclaredConstructor().newInstance();
                  invokePostConstructor(newInstance);
                  return newInstance;

              }
          }
          return findImplementation(clazz);
      } catch (Exception e) {
          System.out.println("something went wrong here it's message : " + e.getMessage());
          throw new RuntimeException(e);
      }
    }


    private Object findImplementation(Class<?> parameterType) {
        System.out.println("find implementation for " + parameterType.getSimpleName());

            for(Object createScoop : context.values()) {
                if(parameterType.isAssignableFrom(createScoop.getClass())) {
                    System.out.println("🍦 [ПОЛИМОРФИЗМ] Для интерфейса " + parameterType.getSimpleName()
                            + " нашли реализацию-синглтон: " + createScoop.getClass().getSimpleName());
                    return createScoop;
                }
            }
        System.out.println("here's size of prototypes : " + prototypes.size());
            for(Class<?> protoClass : prototypes) {
                if(parameterType.isAssignableFrom(protoClass)) {
                    Object object1 = createOrGetScoop(protoClass);
                    System.out.println("🍦 [ПОЛИМОРФИЗМ] Для интерфейса " + parameterType.getSimpleName()
                                + " нашли реализацию-прототип: " + protoClass.getSimpleName());

                        return object1;

                    }
                }

        return null;
    }


    private boolean isScoop(Class<?> clazz) {
        for(Annotation anotation : clazz.getAnnotations()) {
            if(anotation.annotationType() == Component.class) {
                return true;
            }
            if(anotation.annotationType().isAnnotationPresent(Component.class)) {
                return true;
            }
        }
        return false;
    }


}

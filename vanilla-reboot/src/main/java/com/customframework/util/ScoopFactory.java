package com.customframework.util;

import com.customframework.Scope;
import com.customframework.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class ScoopFactory {
    private static final Logger log = LoggerFactory.getLogger(ScoopFactory.class);

    private final Map<Class<?>, Object> context = new HashMap<>();
    private final List<Class<?>> complexContext = new ArrayList<>();
    private final List<Class<?>> prototypes = new ArrayList<>();
    private final List<Method> methodsWithArgs = new ArrayList<>();
    private final List<Method> methodsWithoutArgs = new ArrayList<>();
    private final Map<Class<?>,Object> temporaryContext = new HashMap();
    private final Properties properties = new Properties();
    private final Deque<Object> preDestroyQueue = new ArrayDeque<>();



    public List<Class<?>> getPrototypes() {
        return prototypes;
    }


    public ScoopFactory () {
        System.out.println("ScoopFactory");
        loadProperties();
    }


    private void loadProperties() {
        try(InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if(input != null) {
                properties.load(input);
                System.out.println("⚙️ Загружен application.properties! Найдено ключей: " + properties.size());
            } else {
                System.out.println("⚠️ Файл application.properties не найден в resources!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки application.properties", e);
        }
    }




    public Map<Class<?>, Object> instantiate(List<Class<?>> componentClasses) {

        registerShutdownHook();

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
                        injectValueFields(object);
                        invokePostConstructor(object);
                        registerScoopsWithPreDestroy(object);
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
                        injectValueFields(object);
                        System.out.println("🍦 Успешно создали scoop: " + clazz.getSimpleName());// <-- ДОБ
                        invokePostConstructor(object);
                        registerScoopsWithPreDestroy(object);
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


            invokeConfigurations();
            System.out.println("this is our prototypes size : " + prototypes.size());
            context.putAll(temporaryContext);



            return context;

        } catch (Exception e) {
            log.error("Scoop instantiation failed -> : ", e.getMessage());
            throw new RuntimeException(e.getMessage());
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
                      injectValueFields(complexObject);
                      invokePostConstructor(complexObject);
                      return complexObject;

                  }

              } else {
                  Object newInstance = clazz.getDeclaredConstructor().newInstance();
                  injectValueFields(newInstance);
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

            if(clazz.isAnnotationPresent(Configuration.class)) {
                return true;
            }
        }
        return false;
    }






//    private void retrieveMethodsAnnotatedWithScoop(Class<?> clazz) {
//        try {
//            Object object = clazz.getDeclaredConstructor().newInstance();
//            for (Method method : clazz.getDeclaredMethods()) {
//                if (method.isAnnotationPresent(Scoop.class)) {
//                    System.out.println("the class name that is annotated with config is : " + clazz.getSimpleName());
//                    List<Object> list = handleMethodArgs(method);
//                    method.invoke(object, list.toArray());
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("something went wrong here it's message : " + e.getMessage());
//            throw new RuntimeException(e);
//        }
//    }


    private List<Object> handleMethodWithArgs(Method method) {
        System.out.println("🐘 this method is handleMethodWithArgs !");
        List<Object> arguments = new ArrayList<>();

        for(Parameter parameter : method.getParameters()) {
            System.out.println(" 😼 method name : " + method.getName() + " , method parameters : " + parameter.getType().getSimpleName());
            Object scoop = createOrGetScoop(parameter.getType());
            arguments.add(scoop);

        }
        return arguments;
    }





    private void handleMethodWithoutArgs(Object object) {
        System.out.println("🐬 this method is handleMethodWithoutArgs !");
        try {
            for(Method m : methodsWithoutArgs) {
                if(m.getReturnType() == void.class) {
                    m.invoke(object);
                } else {
                    Object object1 = m.invoke(object);
//                    context.put(m.getReturnType(), object1);
                    temporaryContext.put(m.getReturnType(), object1);
                    injectValueFields(object1);
                    invokePostConstructor(object1);
                    registerScoopsWithPreDestroy(object1);
                }

            }



        } catch (Exception e) {
            System.out.println("something went wrong here it's message : " + e.getMessage());
            throw new RuntimeException(e);
        }

    }



    private void sortMethods(Class<?> clazz) {
        System.out.println("👾 this method is sortMethods !");
        methodsWithoutArgs.clear();
        methodsWithArgs.clear();

        try {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Scoop.class)) {
                    if(method.getParameters().length >= 1) {
                        methodsWithArgs.add(method);
                    }
                    else{
                        methodsWithoutArgs.add(method);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("something went wrong here it's message : " + e.getMessage());
            throw new RuntimeException(e);
        }

    }


    private void handleConfiguration(Object object) {
        System.out.println("😶‍🌫️ this method is handleConfiguration !");
        try {

            sortMethods(object.getClass());
            handleMethodWithoutArgs(object);

            System.out.println("size of methods : " + methodsWithoutArgs.size());

            for(Method method : methodsWithArgs) {
                List<Object> list = handleMethodWithArgs(method);
                System.out.println("method name : " + method.getName() + " , method parameters : " + list);
                Object object1 = method.invoke(object, list.toArray());
                System.out.println("is object is null ? : " + object1);

                System.out.println("🤓🤓🤓🤓🤓🤓 -> " + object1);

                 temporaryContext.put(method.getReturnType(),object1);
                 injectValueFields(object1);
                 invokePostConstructor(object1);
                 registerScoopsWithPreDestroy(object1);

//                context.put(object1.getClass(), object1);


                System.out.println("the endo of 🔚🔚🔚");
            }
            System.out.println("the endo of 🔚🔚🔚");


        } catch (Exception e) {
            System.out.println("something went wrong here it's message : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }



      private void invokeConfigurations() {
            System.out.println("🤢 this method is invokeConfigurations !");
            for (Object object : context.values()) {
                System.out.println("welcome to configuration : " + object);
                Class<?> clazz = object.getClass();
                if (clazz.isAnnotationPresent(Configuration.class)) {
                    handleConfiguration(context.get(clazz));
                    System.out.println("🔍 this is the end of invokeConfigurations ! 👾");

                }
                System.out.println("context object : " + clazz.getSimpleName());

            }
            System.out.println("🔍 this is the end of invokeConfigurations !");
    }




    private void injectValueFields(Object scoop) {
        Class<?> clazz = scoop.getClass();

        for(Field field : clazz.getDeclaredFields()) {
            if(field.isAnnotationPresent(Value.class)) {


                Value annotation = field.getAnnotation(Value.class);
                String propertyKey = annotation.value();

                if(propertyKey.startsWith("${") && propertyKey.endsWith("}")) {
                    propertyKey = propertyKey.substring(2, propertyKey.length() - 1);
                }

                String rawValue = properties.getProperty(propertyKey);

                if(rawValue == null) {
                    throw new IllegalStateException("Property key '" + propertyKey + "' not found for field " + field.getName());
                }

                Object convertedValue = convertType(field.getType(), rawValue);

                try{
                    field.setAccessible(true);
                    field.set(scoop,convertedValue);
                    System.out.println("⚙️ Внедрено значение " + propertyKey + " = " + convertedValue + " в " + clazz.getSimpleName());
                } catch (Exception e) {
                    throw new RuntimeException("Не удалось внедрить @Value в поле " + field.getName(), e);
                }
            }
        }
    }


    private Object convertType(Class<?> targetType, String value) {
        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);

        throw new IllegalArgumentException("Неподдерживаемый тип поля для @Value: " + targetType.getName());
    }


    private void registerScoopsWithPreDestroy(Object object) {
          Class<?> clazz = object.getClass();
          for(Method m : clazz.getDeclaredMethods()) {
              if(m.isAnnotationPresent(PreDestroy.class)) {
                  if(m.getParameters().length >= 1) {
                      throw new IllegalStateException("Метод @PreDestroy в классе " + clazz.getSimpleName() +
                              " не должен иметь параметры");
                  }

                  if(m.getReturnType() != void.class) {
                      throw new IllegalStateException("Метод @PreDestroy в классе " +
                              clazz.getSimpleName() + " не должен быть возвращающим!");
                  }

                  preDestroyQueue.add(object);
              }
          }
    }


    public void destroy() {
        Iterator<Object> iterator = preDestroyQueue.descendingIterator();

        while(iterator.hasNext()) {

            Object object = iterator.next();

            for(Method method : object.getClass().getDeclaredMethods()) {
                if(method.isAnnotationPresent(PreDestroy.class)) {
                    try {
                        method.setAccessible(true);
                        method.invoke(object);

                    } catch (Exception e) {
                        throw new RuntimeException("Не удалось вызвать @PreDestroy для бина "
                                + object.getClass().getName(), e);
                    }
                }
            }
        }

    }


    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\\n[Hook] JVM завершает работу! Выполняем очистку...");
            destroy();
        } ));
    }


}

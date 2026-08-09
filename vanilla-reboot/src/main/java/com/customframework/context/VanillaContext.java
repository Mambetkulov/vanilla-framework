package com.customframework.context;

import com.customframework.annotation.Component;


import java.util.*;

import com.customframework.util.ClassPathScanner;
import com.customframework.util.ScoopFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VanillaContext {
    private static final Logger log = LoggerFactory.getLogger(VanillaContext.class);
    private final Map<Class<?>, Object> context;
    private final ScoopFactory scoopFactory;





    public VanillaContext(Class<?> contextClass) {

        ClassPathScanner scanner = new ClassPathScanner();
        List<Class<?>> foundClasses = scanner.scan(contextClass);

        scoopFactory = new ScoopFactory();
        this.context = scoopFactory.instantiate(foundClasses);

        log.info("VanillaContext initialized successfully with {} scoops!", context.size());
    }





    @SuppressWarnings("unchecked")
    public <T> T getItem(Class<T> contextClass) {
        Object object = scoopFactory.createOrGetScoop(contextClass);
        return (T) object;
    }

    public Map<Class<?>, Object> getMap() {

        return context;
    }


    public ScoopFactory getScoopFactory() {
        return scoopFactory;
    }

}

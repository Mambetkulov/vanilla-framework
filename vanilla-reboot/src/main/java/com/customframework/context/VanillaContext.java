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




    public VanillaContext(Class<?> contextClass) {

        ClassPathScanner scanner = new ClassPathScanner();
        List<Class<?>> foundClasses = scanner.scan(contextClass);

        ScoopFactory scoopFactory = new ScoopFactory();
        this.context = scoopFactory.instantiate(foundClasses);

        log.info("VanillaContext initialized successfully with {} scoops!", context.size());
    }





    @SuppressWarnings("unchecked")
    public <T> T getItem(Class<T> contextClass) {
        return (T) context.get(contextClass);
    }
}

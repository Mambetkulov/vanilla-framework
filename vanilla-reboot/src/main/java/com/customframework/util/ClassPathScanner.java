package com.customframework.util;

import com.customframework.error.ScannerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class ClassPathScanner {
    private static final Logger log = LoggerFactory.getLogger(ClassPathScanner.class);



    public List<Class<?>> scan(Class<?> rootClass) {
        String packageName = rootClass.getPackageName();
        String packagePath = packageName.replace(".", "/");


        try {

            log.info("scanning packages ...");

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(packagePath);

            if (resource == null) {
                log.warn("could not find resource {}", packagePath);
                throw new NullPointerException("could not find resource " + packagePath);
            }

            File rootFolder = new File(resource.toURI());

            List<Class<?>> foundClasses = new ArrayList<>();
            findClasses(rootFolder, packageName, foundClasses);

            log.info("total found classes : {}", foundClasses.size());

            return foundClasses;

        }
        catch (Exception e) {
            log.error("Scanning failed for package: {}", packageName, e);
            throw new ScannerException("Failed to scan classpath");
        }
    }



    private void findClasses(File directory, String packageName, List<Class<?>> classes) throws Exception {
        if(!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();

        if(files == null) {
            return;
        }

        for(File file : files) {
            if(file.isDirectory()) {
                findClasses(file, packageName + "." + file.getName(), classes);
            }
            else {
                if(!file.getName().endsWith(".class")) {
                    continue;
                }

                String pureClassName = file.getName().substring(0, file.getName().length() - 6);
                String className = packageName + "." + pureClassName;
                Class<?> clazz = Class.forName(className);
                classes.add(clazz);
            }
        }
    }

}

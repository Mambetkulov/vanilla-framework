package com.customframework.config;

import com.customframework.annotation.Component;
import com.customframework.annotation.PostConstructor;
import com.customframework.annotation.PreDestroy;
import com.customframework.annotation.Value;

@Component
public class AppServer {

    @Value("app.name")
    private String appName;

    @Value("app.port")
    private int port;

    @Value("${app.debug}") // Тестируем синтаксис с ${...}
    private boolean debugMode;

    @Value("app.timeout")
    private Long timeout;

    @PostConstructor
    public void init() {
        System.out.println("\n🚀 --- ПРОВЕРКА @Value В @PostConstruct ---");
        System.out.println("App Name   : " + appName);
        System.out.println("Port       : " + port + " (тип: " + ((Object)port).getClass().getSimpleName() + ")");
        System.out.println("Debug Mode : " + debugMode);
        System.out.println("Timeout    : " + timeout + " ms");
        System.out.println("----------------------------------------\n");
    }

    // Геттеры для проверки в assert'ах
    public String getAppName() { return appName; }
    public int getPort() { return port; }
    public boolean isDebugMode() { return debugMode; }
    public Long getTimeout() { return timeout; }


    @PreDestroy
    public void close() {
        System.out.println("it is pre-destroy method of AppServer 👴");
    }

}

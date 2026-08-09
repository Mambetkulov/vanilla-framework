package com.customframework.service;

import com.customframework.annotation.Component;

@Component
public class SingletonService {

    public SingletonService() {
        System.out.println("📦 [INIT] Создан SingletonService");
    }
}

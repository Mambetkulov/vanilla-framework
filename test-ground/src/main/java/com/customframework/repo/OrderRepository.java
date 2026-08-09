package com.customframework.repo;

import com.customframework.Scope;
import com.customframework.annotation.Component;
import com.customframework.annotation.PostConstructor;
import com.customframework.annotation.Repository;
import com.customframework.annotation.ScoopScope;

@Component
public class OrderRepository {

    public void save() {
        System.out.println("Данные сохранены в бд");
    }


    @PostConstructor
    private void init() {
        System.out.println("PostConstructor of OrderRepository was invoked!");
    }
}

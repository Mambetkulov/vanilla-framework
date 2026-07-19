package com.customframework.repo;

import com.customframework.annotation.Component;
import com.customframework.annotation.PostConstructor;
import com.customframework.annotation.Repository;

@Repository
public class OrderRepository {

    public void save() {
        System.out.println("Данные сохранены в бд");
    }


    @PostConstructor
    private void init() {
        System.out.println("PostConstructor of OrderRepository was invoked!");
    }
}

package com.customframework.repo;

import com.customframework.annotation.Component;

@Component
public class OrderRepository {

    public void save() {
        System.out.println("Данные сохранены в бд");
    }
}

package com.customframework.test;

import com.customframework.annotation.Component;

@Component
public class CaramelTopping implements Topping {
    private final MapleSyrup syrup;

    public CaramelTopping(MapleSyrup syrup) {
        this.syrup = syrup;
    }


    @Override
    public void enjoy() {
        System.out.println("🍯 Поливаем тягучей карамелью с добавлением: " + syrup.getName());
    }
}

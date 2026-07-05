package com.customframework.test;

import com.customframework.annotation.Component;

@Component
public class ChocolateToping implements Topping {

    @Override
    public void enjoy() {
        System.out.println("🍫 Хрустим шоколадной крошкой!");
    }
}

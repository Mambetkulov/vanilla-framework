package com.customframework.controller;

import com.customframework.annotation.Component;
import com.customframework.annotation.Inject;


public class Husk {
    private final Test1 test1;
    private final Test2 test2;

    public Husk(Test1 test1, Test2 test2) {
        this.test1 = test1;
        this.test2 = test2;

    }
}

package com.customframework.repo;

import com.customframework.Scope;
import com.customframework.annotation.Component;
import com.customframework.annotation.ScoopScope;

@ScoopScope(Scope.PROTOTYPE)
@Component
public class TestHello {

    public TestHello() {
        System.out.println("TestHello");
    }
}

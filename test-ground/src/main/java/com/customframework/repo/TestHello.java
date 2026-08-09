package com.customframework.repo;

import com.customframework.Scope;
import com.customframework.annotation.Component;
import com.customframework.annotation.Inject;
import com.customframework.annotation.ScoopScope;

@ScoopScope(Scope.PROTOTYPE)
@Component
public class TestHello {

    @Inject
    public TestHello(TestRepository testRepository) {
        System.out.println("😼TestHello : this is testRepository : " + testRepository);
    }
}

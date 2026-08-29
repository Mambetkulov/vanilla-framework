package com.customframework.test;

import com.customframework.repo.TestHello;

public class RandomObject {
    private final TestHello testHello;

    public RandomObject(TestHello testHello) {
        this.testHello = testHello;
        System.out.println("🤓 this is a random object and its dependency is  " + this.testHello);
    }

    public TestHello getTestHello() {
        return testHello;
    }
}

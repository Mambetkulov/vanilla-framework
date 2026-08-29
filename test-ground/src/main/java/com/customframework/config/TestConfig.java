package com.customframework.config;

import com.customframework.annotation.Configuration;
import com.customframework.annotation.Scoop;
import com.customframework.repo.TestHello;
import com.customframework.test.RandomObject;

import java.util.Scanner;

@Configuration
public class TestConfig {

    @Scoop
    public RandomObject testMethod(TestHello testHello) {
        System.out.println("testMethod was executed 🫡");
        return new RandomObject(testHello);
    }

    @Scoop
    public Scanner testMethod2() {
        System.out.println("testMethod2 was executed 🫡");
        return new Scanner(System.in);
    }
}

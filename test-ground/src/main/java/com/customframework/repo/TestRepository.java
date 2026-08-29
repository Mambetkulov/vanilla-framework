package com.customframework.repo;

import com.customframework.annotation.PreDestroy;
import com.customframework.annotation.Repository;

@Repository
public class TestRepository {

    public TestRepository() {
        System.out.println("TestRepository");
    }


    @PreDestroy
    public void close() {
        System.out.println("🩷 it is a preDestroy method of TestRepository");
    }
}

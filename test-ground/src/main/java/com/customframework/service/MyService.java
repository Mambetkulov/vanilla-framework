package com.customframework.service;

import com.customframework.annotation.Component;
import com.customframework.annotation.Inject;
import com.customframework.annotation.Service;
import com.customframework.repo.TestHello;

@Service
public class MyService {


    public MyService(TestHello testHello) {
        System.out.println("MyService constructor");
    }
}

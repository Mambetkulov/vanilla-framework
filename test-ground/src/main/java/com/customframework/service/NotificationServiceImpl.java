package com.customframework.service;

import com.customframework.annotation.Component;

@Component
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendNotification(String message) {
        System.out.println("Email: " + message);
    }
}

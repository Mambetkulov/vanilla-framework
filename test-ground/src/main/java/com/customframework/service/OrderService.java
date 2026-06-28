package com.customframework.service;

import com.customframework.annotation.Component;
import com.customframework.annotation.Inject;
import com.customframework.repo.OrderRepository;

@Component
public class OrderService {
//    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Inject
    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void sendMessage(String message) {
        notificationService.sendNotification(message);
    }

    public void CreateRepo () {
//        System.out.println("Создаем заказ...");
//        orderRepository.save();
    }
}

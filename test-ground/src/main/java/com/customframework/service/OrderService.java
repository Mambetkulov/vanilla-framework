package com.customframework.service;

import com.customframework.annotation.Component;
import com.customframework.annotation.Inject;
import com.customframework.repo.OrderRepository;

@Component
public class OrderService {
    private final OrderRepository orderRepository;

    @Inject
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void CreateRepo () {
        System.out.println("Создаем заказ...");
        orderRepository.save();
    }
}

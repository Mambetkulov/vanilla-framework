package com.customframework.test;

import com.customframework.annotation.Component;

import java.util.List;

@Component
public class IceCreamOrderService {
    private final List<Topping> toppings;

    // Сюда Vanilla должна засунуть и Шоколад, и Карамель,
    // дождавшись, пока Карамель доварится!
    public IceCreamOrderService(List<Topping> toppings) {
        this.toppings = toppings;
    }

    public void serve() {
        System.out.println("\n=== 🍦 ПОДАЧА МОРОЖЕНОГО ===");
        System.out.println("Добавлено топпингов: " + toppings.size());
        for (Topping topping : toppings) {
            topping.enjoy();
        }
    }
}

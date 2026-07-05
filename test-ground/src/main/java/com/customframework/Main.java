package com.customframework;

import com.customframework.context.VanillaContext;
import com.customframework.service.OrderService;
import com.customframework.test.IceCreamOrderService;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
      VanillaContext context = Vanilla.start(Main.class);
      System.out.println("=== 🚀 ЗАПУСК КОНТЕКСТА VANILLA ===");

      System.out.println("=== ✅ КОНТЕКСТ УСПЕШНО ПОДНЯТ ===\n");
      OrderService service = context.getItem(OrderService.class);
      service.sendMessage("Hello World");

      IceCreamOrderService orderService = (IceCreamOrderService) context.getItem(IceCreamOrderService.class);
      orderService.serve();





    }
}
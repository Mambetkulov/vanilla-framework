package com.customframework;

import com.customframework.context.VanillaContext;
import com.customframework.repo.TestHello;
import com.customframework.service.OrderService;
import com.customframework.service.PrototypeCommand;
import com.customframework.service.SingletonService;
import com.customframework.test.IceCreamOrderService;
import com.customframework.util.ScoopFactory;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
      VanillaContext context = Vanilla.start(Main.class);
      ScoopFactory scoopFactory = context.getScoopFactory();
      System.out.println("=== 🚀 ЗАПУСК КОНТЕКСТА VANILLA ===");

      System.out.println("size of prototypes : " + scoopFactory.getPrototypes().size());

      System.out.println("=== ✅ КОНТЕКСТ УСПЕШНО ПОДНЯТ ===\n");
      OrderService service = context.getItem(OrderService.class);
      service.sendMessage("Hello World");
      System.out.println("context size : " + context.getMap().size());
      IceCreamOrderService orderService = (IceCreamOrderService) context.getItem(IceCreamOrderService.class);
      orderService.serve();

      System.out.println("");

      for(Object object : context.getMap().values()) {
        System.out.println("-->" + object.getClass().getName());
      }

      System.out.println("\n--- ТЕСТ 1: Проверка Синглтона ---");
      Object s1 = scoopFactory.createOrGetScoop(SingletonService.class);
      Object s2 = scoopFactory.createOrGetScoop(SingletonService.class);

      System.out.println("Ссылки синглтонов равны? " + (s1 == s2));
      // Ожидание: true (объект один и тот же)

      System.out.println("\n--- ТЕСТ 2: Проверка Прототипа ---");
      Object p1 = scoopFactory.createOrGetScoop(PrototypeCommand.class);
      Object p2 = scoopFactory.createOrGetScoop(PrototypeCommand.class);

      System.out.println("prototypes size : " + scoopFactory.getPrototypes().size());

      System.out.println("p1 = " + p1);
      System.out.println("p2 = " + p2);
      System.out.println("Ссылки равны? " + (p1 == p2));

    }
}
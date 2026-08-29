package com.customframework;

import com.customframework.config.AppServer;
import com.customframework.context.VanillaContext;
import com.customframework.repo.TestHello;
import com.customframework.repo.TestRepository;
import com.customframework.service.OrderService;
import com.customframework.service.PrototypeCommand;
import com.customframework.service.SingletonService;
import com.customframework.test.IceCreamOrderService;
import com.customframework.test.RandomObject;
import com.customframework.util.ScoopFactory;

import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
      System.out.println("hello world");
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
      TestRepository testRepository = context.getItem(TestRepository.class);

      System.out.println("this is our repo : " + testRepository);

      Scanner scanner = context.getItem(Scanner.class);
      Scanner scanner2 = context.getItem(Scanner.class);

      System.out.println("are these two scans equal : " + (scanner2 == scanner));
      System.out.println("the value of the scan : " + scanner2.hashCode());
      System.out.println("the value of the second scan : " + scanner.hashCode());



      RandomObject randomObject = context.getItem(RandomObject.class);
      System.out.println("random object : " + randomObject + " and its dependency :" + randomObject.getTestHello());

      AppServer appServer = context.getItem(AppServer.class);
      System.out.println("checking through getter method {name of app} -> " + appServer.getAppName());

      System.out.println("type something you fucking idiot 🌻");
      String s = scanner.nextLine();
      System.out.println("here's your input idiot -> " + s);

    }
}
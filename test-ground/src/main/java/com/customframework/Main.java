package com.customframework;

import com.customframework.context.VanillaContext;
import com.customframework.service.OrderService;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
      VanillaContext context = Vanilla.start(Main.class);
      OrderService service = context.getItem(OrderService.class);
      service.CreateRepo();

    }
}
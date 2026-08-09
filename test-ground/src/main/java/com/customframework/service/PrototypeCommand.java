package com.customframework.service;

import com.customframework.Scope;
import com.customframework.annotation.Component;
import com.customframework.annotation.ScoopScope;

@ScoopScope(Scope.PROTOTYPE)
@Component
public class PrototypeCommand {

    public PrototypeCommand() {
        System.out.println("⚡ [INIT] Создан НОВЫЙ PrototypeCommand");
    }
}

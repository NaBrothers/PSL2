package com.nabrothers.psl.server.manager;

import com.nabrothers.psl.server.context.HandlerContext;
import org.springframework.stereotype.Component;

@Component
public class PluginManager {
    private HandlerContext context = HandlerContext.getInstance();

    public void init() {
        context.load("com.nabrothers.psl.server");
    }
}

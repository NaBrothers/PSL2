package com.nabrothers.psl.core.controller;

import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Hidden;
import org.springframework.stereotype.Component;

@Component
@Hidden
public class TestController {
    @Handler(command = "心跳")
    public String heartbeat() {
        return "ok";
    }

    @Handler(command = "echo")
    public String echo(String arg) {
        return arg;
    }


}

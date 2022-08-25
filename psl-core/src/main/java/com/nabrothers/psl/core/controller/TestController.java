package com.nabrothers.psl.core.controller;

import com.nabrothers.psl.sdk.annotation.Handler;
import org.springframework.stereotype.Controller;

@Controller
public class TestController {
    @Handler(command = "心跳")
    public String heartbeat() {
        return "ok";
    }

    @Handler(command = "测试")
    public String echo(String arg) {
        return arg;
    }
}

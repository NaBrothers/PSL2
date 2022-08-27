package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Hidden;
import org.springframework.stereotype.Controller;

@Controller
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

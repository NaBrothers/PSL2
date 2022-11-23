package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.exception.TransactionException;
import com.nabrothers.psl.core.service.TransactionService;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

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

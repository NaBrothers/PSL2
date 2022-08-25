package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.utils.HttpUtils;
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

    @Handler(command = "啥是")
    public String whatis(String arg) {
        String retStr = HttpUtils.doGet(String.format("https://baike.baidu.com/api/openapi/BaikeLemmaCardApi?appid=%d&bk_key=%s", 379020, arg));
        String answer = "";
        if (retStr != null) {
            JSONObject ret = JSON.parseObject(retStr);
            if (!ret.isEmpty()) {
                answer = ret.getString("abstract");
            } else {
                answer = "你搁这说什么b话呢";
            }
        }
        return answer;
    }
}

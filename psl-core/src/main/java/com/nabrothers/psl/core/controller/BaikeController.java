package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import org.springframework.stereotype.Controller;

@Controller
@Handler(command = "啥是")
public class BaikeController {
    @Handler(info = "问懂哥问题，懂哥啥都懂")
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

package com.nabrothers.psl.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Param;
import com.nabrothers.psl.sdk.message.ImageMessage;
import com.nabrothers.psl.sdk.service.CacheService;
import com.nabrothers.psl.server.utils.HttpUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DrawContoller {
    @Resource
    private CacheService cacheService;

    @Handler(command = "画画", info = "AI绘画，英文效果更佳")
    public ImageMessage draw(@Param("提示语") String prompt) {
        ImageMessage message = new ImageMessage();
        JSONObject header = new JSONObject();
        String API_TOKEN = cacheService.get("config", "openai_token");
        header.put("Authorization", "Bearer " + API_TOKEN);

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("prompt", prompt);
        jsonObj.put("n", 1);
        jsonObj.put("size", "256x256");
        String retStr = HttpUtils.doPostWithProxy("https://api.openai.com/v1/images/generations", jsonObj, header);
        JSONObject result = JSONObject.parseObject(retStr);
        message.setUrl(result.getJSONArray("data").getJSONObject(0).getString("url"));
        return message;
    }
}

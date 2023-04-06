package com.nabrothers.psl.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Hidden;
import com.nabrothers.psl.sdk.service.CacheService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
@Hidden
public class CacheController {
    @Resource
    private CacheService cacheService;

    @Handler(command = "get")
    public String get(String category, String key) {
        return cacheService.get(category, key);
    }

    @Handler(command = "put")
    public String put(String category, String key, String value) {
        cacheService.put(category, key, value);
        return "ok";
    }

    @Handler(command = "del")
    public String delete(String category, String key) {
        cacheService.delete(category, key);
        return "ok";
    }

    @Handler(command = "getAll")
    public String getAll(String category) {
        Map<String, String> result = cacheService.getAll(category);
        return JSONObject.toJSONString(result);
    }
}

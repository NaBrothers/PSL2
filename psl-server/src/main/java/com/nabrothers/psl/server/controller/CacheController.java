package com.nabrothers.psl.server.controller;

import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Hidden;
import com.nabrothers.psl.sdk.service.CacheService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
}

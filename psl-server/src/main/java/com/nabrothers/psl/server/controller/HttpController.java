package com.nabrothers.psl.server.controller;

import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Hidden;
import com.nabrothers.psl.server.utils.HttpUtils;
import org.springframework.stereotype.Component;

@Component
@Hidden
public class HttpController {
    @Handler(command = "curl")
    public String curl(String url) {
        if (!url.startsWith("127.0.0.1") || !url.startsWith("localhost")) {
            throw new RuntimeException("Safety check failed! Only support localhost requests.");
        }
        return HttpUtils.doGet(url);
    }
}

package com.nabrothers.psl.core.controller;

import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.message.ImageMessage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
public class GenshinController {
    @Handler(command = "来个原", info = "原批诱捕器")
    public ImageMessage getGenshin() throws Exception {
        ImageMessage response = new ImageMessage();
        String retStr = HttpUtils.getRedirectUrl("https://api.dujin.org/pic/yuanshen/");
        response.setUrl(retStr);
        return response;
    }
}

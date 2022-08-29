package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.message.VideoMessage;
import org.springframework.stereotype.Controller;

@Controller
public class BeautyController {

    @Handler(command = "来个妹子", info = "随机发送一个妹子视频")
    public VideoMessage getBeauty() {
        VideoMessage response = new VideoMessage();
        String retStr = HttpUtils.doGet("https://api.apiopen.top/api/getMiniVideo?size=1");
        JSONObject ret = JSONObject.parseObject(retStr);
        String url = ret.getJSONObject("result").getJSONArray("list").getJSONObject(0).getString("playurl");
        response.setUrl(url);
        return response;
    }
}

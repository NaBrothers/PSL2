package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.message.ImageMessage;
import org.springframework.stereotype.Controller;

@Controller
public class CarController {

    @Handler(command = "来辆车", info = "随机提车")
    public ImageMessage getCar() {
        ImageMessage message = new ImageMessage();
        String retStr = HttpUtils.doGet("https://api.apiopen.top/api/getImages?type=car&page=0&size=1");
        JSONObject ret = JSONObject.parseObject(retStr);
        JSONObject obj = ret.getJSONObject("result").getJSONArray("list").getJSONObject(0);
        String url = obj.getString("url");
        String title = obj.getString("title");
        String fileName = url.hashCode() + ".jpg";
        HttpUtils.download(url, "./go-cqhttp/data/images/cache/" + fileName);
        message.setUrl("cache/" + fileName);
        message.setInfo(title);
        return message;
    }
}

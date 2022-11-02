package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.message.ImageMessage;
import org.springframework.stereotype.Controller;

import java.util.Random;

@Controller
public class CarController {

    @Handler(command = "来辆车", info = "随机提车")
    public ImageMessage getCar() {
        ImageMessage message = new ImageMessage();
        int page = new Random().nextInt(10000) + 1;
        String retStr = HttpUtils.doGet("http://wallpaper.apc.360.cn/index.php?%20c=WallPaper&a=getAppsByCategory&cid=12&start="+page+"&count=1&from=360chrome");
        JSONObject ret = JSONObject.parseObject(retStr);
        JSONObject obj = ret.getJSONArray("data").getJSONObject(0);
        String url = obj.getString("url");
        String title = obj.getString("utag");
        message.setUrl(url);
        message.setInfo(title);
        return message;
    }
}

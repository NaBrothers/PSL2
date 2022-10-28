package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Param;
import com.nabrothers.psl.sdk.message.ImageMessage;
import org.springframework.stereotype.Controller;

import java.util.Random;

@Controller
public class ImageController {
    @Handler(command = "来个", info = "来个图")
    public ImageMessage getImage(@Param("图片名") String arg) {
        ImageMessage message = new ImageMessage();
        int page = new Random().nextInt(100) + 1;
        String retStr = HttpUtils.doGet("https://image.baidu.com/search/acjson?tn=resultjson_com&ipn=rj&word=" + arg + "&pn=" + page + "&rn=1");
        JSONObject ret = JSONObject.parseObject(retStr);
        String url = ret.getJSONArray("data").getJSONObject(0).getString("middleURL");
        String fileName = url.hashCode() + ".jpg";
        HttpUtils.download(url, "./go-cqhttp/data/images/cache/" + fileName);
        message.setUrl("cache/" + fileName);
        return message;
    }
}

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
        if (url == null) {
            message.setUrl("https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fww1.sinaimg.cn%2Fbmiddle%2F6af89bc8gw1f8r0u46iqtj203w03wdfo.jpg&refer=http%3A%2F%2Fwww.sina.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1669535645&t=ebac5f260710146e5c5ee3d667df8180");
        }
        String fileName = url.hashCode() + ".jpg";
        HttpUtils.download(url, "./go-cqhttp/data/images/cache/" + fileName);
        message.setUrl("cache/" + fileName);
        return message;
    }
}

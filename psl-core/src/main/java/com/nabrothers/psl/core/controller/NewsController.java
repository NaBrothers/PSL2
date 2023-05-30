package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.dto.UserDTO;
import com.nabrothers.psl.core.utils.Config;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Hidden;
import com.nabrothers.psl.sdk.message.CQCode;
import com.nabrothers.psl.sdk.service.MessageService;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class NewsController {
    @Resource
    private MessageService messageService;

    //@Scheduled(cron = "0 0 10 * * ?")
    @Hidden
    @Handler(command = "头条")
    public void dailyNews() {
        messageService.sendGroupMessage(Config.DEFAULT_GROUP_ID, "今日头条");
        List<JSONObject> news = getNews();
        for (JSONObject n : news) {
            String result = String.format(CQCode.SHARE_PATTERN, n.getString("url"), n.getString("title"), n.getString("content"));
            messageService.sendGroupMessage(Config.DEFAULT_GROUP_ID, result);
        }
    }

    private List<JSONObject> getNews() {
        List<JSONObject> result = new ArrayList<>();
        String data = HttpUtils.doGet("http://is.snssdk.com/api/news/feed/v51/");
        JSONObject obj = JSONObject.parseObject(data);
        JSONArray news = obj.getJSONArray("data");
        for (Object n : news) {
            try {
                JSONObject content = ((JSONObject) n).getJSONObject("content");
                JSONObject ret = new JSONObject();
                ret.put("url", content.getString("display_url"));
                ret.put("title", content.get("title"));
                ret.put("content", content.get("abstract"));
                result.add(ret);
            } catch (Exception e) {
                log.error(e);
            }
        }
        return result;
    }

}

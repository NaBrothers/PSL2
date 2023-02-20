package com.nabrothers.psl.core.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.sdk.context.MessageListener;
import com.nabrothers.psl.sdk.context.Session;
import com.nabrothers.psl.sdk.message.CQCode;
import com.nabrothers.psl.sdk.service.CacheService;
import com.nabrothers.psl.sdk.service.MessageService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class KeywordListener implements MessageListener {
    @Resource
    private MessageService messageService;

    @Resource
    private CacheService cacheService;

    @Override
    public void listen(Session session) {
        String message = session.getMessage();
        String value = cacheService.get("config", "keyword");
        if (value == null) {
            return;
        }
        JSONObject obj = JSON.parseObject(value);
        for (String keyword : obj.keySet()) {
            if (message.contains(keyword)) {
                messageService.send(session, String.format(CQCode.AT_PATTERN, session.getSender().getId()) + obj.getString(keyword));
            }
        }
    }
}

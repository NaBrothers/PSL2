package com.nabrothers.psl.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.nabrothers.psl.sdk.context.Session;
import com.nabrothers.psl.sdk.context.SessionContext;
import com.nabrothers.psl.sdk.message.Message;
import com.nabrothers.psl.sdk.message.TextMessage;
import com.nabrothers.psl.sdk.service.CacheService;
import com.nabrothers.psl.server.service.DefaultReplyService;
import com.nabrothers.psl.server.utils.HttpUtils;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Log4j2
public class DefaultReplyServiceImpl implements DefaultReplyService {

    @Resource
    private CacheService cacheService;

    @Override
    public Message getReply(String message) {
        TextMessage reply = new TextMessage();
        reply.setSupportImageMode(false);
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("model", "gpt-3.5-turbo");

            Long maxTokens = Long.valueOf(cacheService.get("openai", "max_tokens", "1024"));

            jsonObj.put("max_tokens", maxTokens);
            JSONArray messages = new JSONArray();

            String chatBg = cacheService.get("openai", "background");
            if (null != chatBg){
                JSONObject sysMessage = new JSONObject();
                sysMessage.put("role", "system");
                sysMessage.put("content", chatBg);
                messages.add(sysMessage);
            }

            Session session = SessionContext.get();
            if (session.isReply()) {
                Session replyMessage = SessionContext.get(session.getReplyMessageId());
                while (replyMessage != null) {
                    JSONObject oneMessage = new JSONObject();
                    oneMessage.put("role", replyMessage.getSender().getId().equals(replyMessage.getSelf().getId()) ? "assistant" : "user");
                    oneMessage.put("content", replyMessage.getMessage());
                    messages.add(oneMessage);
                    replyMessage = SessionContext.get(replyMessage.getReplyMessageId());
                }
            }

            JSONObject oneMessage = new JSONObject();
            oneMessage.put("role", "user");
            oneMessage.put("content", message);
            messages.add(oneMessage);
            jsonObj.put("messages", messages);

            JSONObject header = new JSONObject();
            String API_TOKEN = cacheService.get("openai", "token");
            header.put("Authorization", "Bearer " + API_TOKEN);

            String retStr = HttpUtils.doPostWithProxy("https://api.openai.com/v1/chat/completions", jsonObj, header);

            JSONObject result = JSONObject.parseObject(retStr);
            JSONArray choices = result.getJSONArray("choices");

            if (choices == null) {
                JSONObject error = result.getJSONObject("error");
                if (error != null) {
                    String code = error.getString("code");
                    if (code.equals("context_length_exceeded")) {
                        messages = new JSONArray();
                        messages.add(oneMessage);
                        jsonObj.put("messages", messages);
                        retStr = HttpUtils.doPostWithProxy("https://api.openai.com/v1/chat/completions", jsonObj, header);
                        result = JSONObject.parseObject(retStr);
                        choices = result.getJSONArray("choices");
                        reply.setFooter("超出最大上下文上限，使用新上下文");
                    } else {
                        throw new RuntimeException(error.getString("message"));
                    }
                }
            }

            JSONObject choice = choices.getJSONObject(0);

            JSONObject retMsg = choice.getJSONObject("message");
            String text = retMsg.getString("content").replaceAll("\n\n", "\n");
            reply.setData(text);

            String finishReason = choice.getString("finish_reason");
            if (finishReason.equals("length")) {
                reply.setFooter("超出回复上限：" + maxTokens + "，已截断");
            }
        } catch (Exception e) {
            log.error(e);
            reply.setData("啊哦，出错了: " + e.getMessage());
        }
        return reply;
    }
}

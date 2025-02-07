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
            jsonObj.put("model", "deepseek-chat");

            Long maxTokens = Long.valueOf(cacheService.get("deepseek", "max_tokens", "4096"));

            jsonObj.put("max_tokens", maxTokens);
            JSONArray messages = new JSONArray();

            String chatBg = cacheService.get("deepseek", "background");
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
            String API_TOKEN = cacheService.get("deepseek", "token");
            header.put("Authorization", "Bearer " + API_TOKEN);

	    //log.info(jsonObj.toString());
	    //log.info(header.toString());

            String retStr = HttpUtils.doPost("https://api.deepseek.com/chat/completions", jsonObj, header);

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
                        retStr = HttpUtils.doPostWithProxy("https://api.deepseek.com/chat/completions", jsonObj, header);
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
            log.error(getStackTrace(e));
            reply.setData("啊哦，出错了: " + e.getMessage());
        }
        return reply;
    }

    private String getStackTrace(Throwable e) {
        StackTraceElement[] stackElements = e.getStackTrace();
        StringBuilder sb = new StringBuilder();
        if (null != stackElements) {
            for (int i = 0; i < stackElements.length; i++) {
                if (i > 10) {
                    sb.append(String.format("......"));
                    break;
                }
                sb.append(stackElements[i].getClassName());
                sb.append(".").append(stackElements[i].getMethodName());
                sb.append("(").append(stackElements[i].getFileName()).append(":");
                sb.append(stackElements[i].getLineNumber()+")").append("\n");
            }
        }
        return sb.toString();
    }
}

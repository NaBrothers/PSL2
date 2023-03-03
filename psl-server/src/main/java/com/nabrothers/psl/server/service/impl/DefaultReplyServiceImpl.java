package com.nabrothers.psl.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.nabrothers.psl.sdk.message.CQCode;
import com.nabrothers.psl.sdk.message.Message;
import com.nabrothers.psl.sdk.message.SimpleMessage;
import com.nabrothers.psl.sdk.message.TextMessage;
import com.nabrothers.psl.sdk.service.CacheService;
import com.nabrothers.psl.server.service.DefaultReplyService;
import com.nabrothers.psl.server.utils.HttpUtils;
import lombok.extern.log4j.Log4j2;

import org.eclipse.jetty.util.StringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Random;

@Component
@Log4j2
public class DefaultReplyServiceImpl implements DefaultReplyService {

    @Resource
    private CacheService cacheService;

//    @Override
//    public Message getReply(String message) {
//        SimpleMessage reply = new SimpleMessage();
//        reply.setSupportImageMode(false);
//        try {
//            String retStr = HttpUtils.doGet("https://api.qingyunke.com/api.php?key=free&msg=" + URLEncoder.encode(message, "UTF-8"));
//            String content = JSONObject.parseObject(retStr).getString("content");
//            content = content.replaceAll("\\{br\\}", "\n");
//            int index = -1;
//            do {
//                index = content.indexOf("{face:");
//                if (index > -1) {
//                    int i = index + 6;
//                    for (; i < content.length(); i++) {
//                        if (content.charAt(i) == '}') {
//                            break;
//                        }
//                    }
//                    int faceId = Integer.parseInt(content.substring(index + 6, i));
//                    content = content.substring(0, index) + String.format(CQCode.FACE_PATTERN, faceId) + content.substring(i + 1);
//                }
//            } while (index > -1);
//
//            reply.setData(content);
//
//        } catch (Exception ignore) {
//
//        } finally {
//            return reply;
//        }
//    }

    @Override
    public Message getReply(String message) {
        TextMessage reply = new TextMessage();
        reply.setSupportImageMode(false);
        try {
            // String retStr = HttpUtils.doGetWithProxy("http://47.93.214.136/chatgpt-api.php?keys=" + URLEncoder.encode(message, "UTF-8"));
            // String content = JSONObject.parseObject(retStr).getJSONObject("data").getString("html");
            // content = content.replaceAll("\n\n", "\n");
            // reply.setData(content);

            // Double temperature = new Random().nextDouble();
            // String prompt = String.format("$Human:%s\n$懂哥:", message);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("model", "gpt-3.5-turbo");
            jsonObj.put("max_tokens", 2048);
            JSONArray messages = new JSONArray();

            String chatBg = cacheService.get("openai", "background");
            if (null != chatBg){
                JSONObject sysMessage = new JSONObject();
                sysMessage.put("role", "system");
                sysMessage.put("content", chatBg);
                messages.add(sysMessage);
            }

            JSONObject oneMessage = new JSONObject();
            oneMessage.put("role", "user");
            oneMessage.put("content", message);
            messages.add(oneMessage);
            jsonObj.put("messages", messages);
            // jsonObj.put("temperature", 0.5);
            // jsonObj.put("top_p", 1);
            // jsonObj.put("frequency_penalty", 0);
            // jsonObj.put("presence_penalty", 0.6);
            //jsonObj.put("echo", true);
            //jsonObj.put("stop", Arrays.asList("$Human:", "$懂哥:"));

            JSONObject header = new JSONObject();
            String API_TOKEN = cacheService.get("openai", "token");
            header.put("Authorization", "Bearer " + API_TOKEN);

            String retStr = HttpUtils.doPostWithProxy("https://api.openai.com/v1/chat/completions", jsonObj, header);

            JSONArray choices = JSONObject.parseObject(retStr).getJSONArray("choices");
            JSONObject choice = choices.getJSONObject(0);
            JSONObject retMsg = choice.getJSONObject("message");
            String text = retMsg.getString("content").replaceAll("\n\n", "\n");
            reply.setData(URLDecoder.decode(text, "UTF-8"));
            //reply.setFooter(String.format("抽象程度：%d%%", (int)(temperature*100)));
        } catch (Exception e) {
            log.error(e);
            reply.setData("啊哦，出错了: " + e.getMessage());
        }
        return reply;
    }
}

package com.nabrothers.psl.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.sdk.message.CQCode;
import com.nabrothers.psl.sdk.message.Message;
import com.nabrothers.psl.sdk.message.SimpleMessage;
import com.nabrothers.psl.server.service.DefaultReplyService;
import com.nabrothers.psl.server.utils.HttpUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;

@Component
@Log4j2
public class DefaultReplyServiceImpl implements DefaultReplyService {

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
        SimpleMessage reply = new SimpleMessage();
        reply.setSupportImageMode(false);
        try {
            String retStr = HttpUtils.doGetWithProxy("http://47.93.214.136/chatgpt-api.php?keys=" + URLEncoder.encode(message, "UTF-8"));
            String content = JSONObject.parseObject(retStr).getJSONObject("data").getString("html");
            content = content.replaceAll("\n\n", "\n");
            reply.setData(content);
        } catch (Exception e) {
            log.error(e);
            reply.setData("啊哦，出错了");
        } finally {
            return reply;
        }
    }
}

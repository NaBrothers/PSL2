package com.nabrothers.psl.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.server.request.GroupMessageRequest;
import com.nabrothers.psl.server.request.HeartbeatRequest;
import com.nabrothers.psl.server.request.MessageRequest;
import com.nabrothers.psl.server.request.PrivateMessageRequest;
import com.nabrothers.psl.server.request.enums.MessageType;
import com.nabrothers.psl.server.request.enums.PostType;
import com.nabrothers.psl.server.service.RequestService;
import com.nabrothers.psl.server.service.handler.*;
import com.nabrothers.psl.server.utils.RequestUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class RequestServiceImpl implements RequestService {

    Map<EventType, RequestHandler> handlers = new HashMap<>();

    {
        handlers.put(EventType.HEARTBEAT, new HeartbeatHandler());
        handlers.put(EventType.PRIVATE_MESSAGE, new PrivateMessageHandler());
        handlers.put(EventType.GROUP_MESSAGE, new GroupMessageHandler());
    }

    @Override
    public void handleRequest(HttpServletRequest request) {
        JSONObject param = RequestUtils.getJSONParam(request);
        String postTypeStr = param.getString("post_type");
        if (postTypeStr == null) {
            throw new RuntimeException("不是合法的CQHttp请求");
        }
        PostType postType = PostType.getByName(postTypeStr);
        if (postType == null) {
            throw new RuntimeException("不支持的请求类型：" + postTypeStr);
        }
        switch (postType) {
            case MESSAGE:
                handleMessage(param);
                break;
            case META_EVENT:
                handleMetaEvent(param);
                break;
            default:
                handleOthers(param);
        }
    }

    private void handleMessage(JSONObject param) {
        MessageRequest messageRequest = param.toJavaObject(MessageRequest.class);
        MessageType messageType = MessageType.getByName(messageRequest.getMessage_type());
        switch (messageType) {
            case MESSAGE_PRIVATE:
                PrivateMessageRequest privateMessageRequest = param.toJavaObject(PrivateMessageRequest.class);
                handlers.get(EventType.PRIVATE_MESSAGE).doHandle(privateMessageRequest);
                break;
            case MESSAGE_GROUP:
                GroupMessageRequest groupMessageRequest = param.toJavaObject(GroupMessageRequest.class);
                handlers.get(EventType.GROUP_MESSAGE).doHandle(groupMessageRequest);
                break;
        }
    }

    private void handleMetaEvent(JSONObject param) {
        HeartbeatRequest heartbeatRequest = param.toJavaObject(HeartbeatRequest.class);
        handlers.get(EventType.HEARTBEAT).doHandle(heartbeatRequest);
    }

    private void handleOthers(JSONObject param) {

    }
}

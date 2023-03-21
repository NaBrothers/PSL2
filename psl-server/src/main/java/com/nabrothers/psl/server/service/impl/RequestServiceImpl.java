package com.nabrothers.psl.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.sdk.message.CQCode;
import com.nabrothers.psl.sdk.context.Session;
import com.nabrothers.psl.sdk.context.SessionContext;
import com.nabrothers.psl.sdk.dto.UserDTO;
import com.nabrothers.psl.sdk.message.Message;
import com.nabrothers.psl.server.manager.AccountManager;
import com.nabrothers.psl.server.request.*;
import com.nabrothers.psl.sdk.enums.MessageType;
import com.nabrothers.psl.server.request.enums.PostType;
import com.nabrothers.psl.server.service.RequestService;
import com.nabrothers.psl.server.handler.*;
import com.nabrothers.psl.server.utils.RequestUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class RequestServiceImpl implements RequestService {

    Map<EventType, RequestHandler> handlers = new HashMap<>();

    @Resource
    private GroupMessageHandler groupMessageHandler;

    @Resource
    private PrivateMessageHandler privateMessageHandler;

    @Resource
    private HeartbeatHandler heartbeatHandler;

    @Resource
    private AccountManager accountManager;

    @PostConstruct
    private void init() {
        handlers.put(EventType.HEARTBEAT, heartbeatHandler);
        handlers.put(EventType.PRIVATE_MESSAGE, privateMessageHandler);
        handlers.put(EventType.GROUP_MESSAGE, groupMessageHandler);
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
            log.warn("不支持的请求类型：" + postTypeStr);
            return;
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
        Session session = generateSession(messageRequest);
        SessionContext.add(session);
        SessionContext.record(session);
        Message response = null;
        switch (messageType) {
            case MESSAGE_PRIVATE:
                PrivateMessageRequest privateMessageRequest = param.toJavaObject(PrivateMessageRequest.class);
                privateMessageRequest.setMessage(decode(privateMessageRequest.getMessage()));
                privateMessageRequest.setAt(true);
                SessionContext.get().setAt(true);
                response = handlers.get(EventType.PRIVATE_MESSAGE).doHandle(privateMessageRequest);
                break;
            case MESSAGE_GROUP:
                GroupMessageRequest groupMessageRequest = param.toJavaObject(GroupMessageRequest.class);
                if (messageRequest.getMessage().startsWith("[CQ:reply,id=")) {
                    int lastIndex = messageRequest.getMessage().indexOf(']');
                    Long replyId = Long.parseLong(messageRequest.getMessage().substring(13 , lastIndex));
                    SessionContext.get().setReplyMessageId(replyId);
                    messageRequest.setMessage(messageRequest.getMessage().substring(lastIndex + 1));
                }
                if (messageRequest.getMessage().startsWith(String.format(CQCode.AT_PATTERN, accountManager.getCurrentUser().getId()))) {
                    String message = messageRequest.getMessage().replace(String.format(CQCode.AT_PATTERN, accountManager.getCurrentUser().getId()), "").trim();
                    groupMessageRequest.setMessage(message);
                    groupMessageRequest.setAt(true);
                    SessionContext.get().setMessage(message);
                    SessionContext.get().setAt(true);
                }
                SessionContext.get().setGroup(accountManager.getGroup(groupMessageRequest.getGroup_id()));
                groupMessageRequest.setMessage(decode(groupMessageRequest.getMessage()));
                response = handlers.get(EventType.GROUP_MESSAGE).doHandle(groupMessageRequest);
                break;
        }
        if (response != null) {
            Session responseSession = generateSession(response);
            SessionContext.record(responseSession);
        }
        SessionContext.clear();
    }

    private void handleMetaEvent(JSONObject param) {
        HeartbeatRequest heartbeatRequest = param.toJavaObject(HeartbeatRequest.class);
        handlers.get(EventType.HEARTBEAT).doHandle(heartbeatRequest);
    }

    private void handleOthers(JSONObject param) {

    }

    private Session generateSession(MessageRequest request) {
        Session session = new Session();
        UserDTO sender = new UserDTO();
        sender.setId(request.getSender().getUser_id());
        sender.setNickname(request.getSender().getNickname());
        session.setSender(sender);
        session.setMessage(request.getMessage());
        session.setSelf(accountManager.getCurrentUser());
        session.setMessageType(MessageType.getByName(request.getMessage_type()));
        session.setMessageId(request.getMessage_id());
        return session;
    }

    private Session generateSession(Message response) {
        Session session = new Session();
        session.setSender(accountManager.getCurrentUser());
        session.setMessage(response.getRawMessage());
        session.setSelf(accountManager.getCurrentUser());
        session.setMessageType(SessionContext.get().getMessageType());
        session.setMessageId(response.getId());
        session.setAt(SessionContext.get().isAt());
        session.setReplyMessageId(SessionContext.get().getReplyMessageId());
        return session;
    }

    public static String decode(String input) {
        return StringEscapeUtils.unescapeHtml(input);
    }
}

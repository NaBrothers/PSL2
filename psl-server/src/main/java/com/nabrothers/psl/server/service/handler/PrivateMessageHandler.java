package com.nabrothers.psl.server.service.handler;

import com.nabrothers.psl.server.request.CQHttpRequest;
import com.nabrothers.psl.server.request.PrivateMessageRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class PrivateMessageHandler extends MessageHandler{
    private static final String LOG_SYNTAX = "收到好友 %s(%d) 的消息: %s";

    @Override
    public void doHandle(CQHttpRequest request) {
        PrivateMessageRequest messageRequest = (PrivateMessageRequest) request;
        log.info(String.format(LOG_SYNTAX, messageRequest.getSender().getNickname(), messageRequest.getSender().getUser_id(), messageRequest.getMessage()));
    }
}

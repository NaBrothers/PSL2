package com.nabrothers.psl.server.handler;

import com.nabrothers.psl.server.context.HandlerContext;
import com.nabrothers.psl.server.request.CQHttpRequest;
import com.nabrothers.psl.server.request.PrivateMessageRequest;
import com.nabrothers.psl.server.service.MessageService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Log4j2
public class PrivateMessageHandler extends MessageHandler{
    private static final String LOG_SYNTAX = "收到好友 %s(%d) 的消息: %s";

    @Resource
    private MessageService messageService;

    private HandlerContext context = HandlerContext.getInstance();

    @Override
    public void doHandle(CQHttpRequest request) {
        PrivateMessageRequest messageRequest = (PrivateMessageRequest) request;
        log.info(String.format(LOG_SYNTAX, messageRequest.getSender().getNickname(), messageRequest.getSender().getUser_id(), messageRequest.getMessage()));
        String result = "";
        try {
            result = context.handle(messageRequest.getMessage());
            result = StringUtils.stripEnd(result, "\n");
        } catch (Exception e) {
            log.error(e);
            result = e.getMessage();
        } finally {
            if (StringUtils.isNotEmpty(result)) {
                Long msgId = messageService.sendPrivateMessage(messageRequest.getUser_id(), result);
            }
        }
    }
}

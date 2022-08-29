package com.nabrothers.psl.server.handler;

import com.nabrothers.psl.sdk.message.CQCodeMessage;
import com.nabrothers.psl.sdk.message.Message;
import com.nabrothers.psl.sdk.message.SimpleMessage;
import com.nabrothers.psl.server.config.GlobalConfig;
import com.nabrothers.psl.server.context.HandlerContext;
import com.nabrothers.psl.sdk.message.CQCode;
import com.nabrothers.psl.server.request.CQHttpRequest;
import com.nabrothers.psl.server.request.PrivateMessageRequest;
import com.nabrothers.psl.server.service.MessageService;
import com.nabrothers.psl.server.utils.ImageUtils;
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
        Message message = null;
        try {
            Object result = context.handle(messageRequest.getMessage());
            if (result instanceof Message) {
                message = (Message) result;
            } else {
                message = new SimpleMessage(result.toString());
            }
        } catch (Exception e) {
            log.error(e);
            message = new SimpleMessage(e.getMessage());
        } finally {
            String response = "";
            if (GlobalConfig.ENABLE_IMAGE_MODE) {
                if (message instanceof CQCodeMessage) {
                    response = message.getMessage();
                } else {
                    String path = ImageUtils.toImage(message.getMessage());
                    response = String.format(CQCode.IMAGE_PATTERN, path);
                }
            } else {
                response = message.getRawMessage();
            }

            if (StringUtils.isNotEmpty(response)) {
                Long msgId = messageService.sendPrivateMessage(messageRequest.getUser_id(), response);
            }
        }
    }
}

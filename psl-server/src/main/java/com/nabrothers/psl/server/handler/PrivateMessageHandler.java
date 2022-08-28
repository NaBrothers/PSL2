package com.nabrothers.psl.server.handler;

import com.nabrothers.psl.server.config.GlobalConfig;
import com.nabrothers.psl.server.context.HandlerContext;
import com.nabrothers.psl.server.request.CQCode;
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
        String message = "";
        try {
            Object result = context.handle(messageRequest.getMessage());
            message = result.toString();
            message = StringUtils.stripEnd(message, "\n");
        } catch (Exception e) {
            log.error(e);
            message = e.getMessage();
        } finally {
            if (StringUtils.isNotEmpty(message)) {
                if (GlobalConfig.ENABLE_IMAGE_MODE) {
                    String path = ImageUtils.toImage(message);
                    message = String.format(CQCode.IMAGE_PATTERN, path);
                }
                Long msgId = messageService.sendPrivateMessage(messageRequest.getUser_id(), message);
            }
        }
    }
}

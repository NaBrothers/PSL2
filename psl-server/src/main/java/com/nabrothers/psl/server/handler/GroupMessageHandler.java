package com.nabrothers.psl.server.handler;

import com.nabrothers.psl.server.config.GlobalConfig;
import com.nabrothers.psl.server.context.HandlerContext;
import com.nabrothers.psl.server.dto.GroupDTO;
import com.nabrothers.psl.server.manager.AccountManager;
import com.nabrothers.psl.server.request.CQCode;
import com.nabrothers.psl.server.request.CQHttpRequest;
import com.nabrothers.psl.server.request.GroupMessageRequest;
import com.nabrothers.psl.server.service.MessageService;
import com.nabrothers.psl.server.utils.ImageUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Log4j2
public class GroupMessageHandler extends MessageHandler{
    private static final String LOG_SYNTAX = "收到群 %s(%d) 内 %s(%d) 的消息: %s";

    @Resource
    private AccountManager accountManager;

    @Resource
    private MessageService messageService;

    private HandlerContext context = HandlerContext.getInstance();

    @Override
    public void doHandle(CQHttpRequest request) {
        GroupMessageRequest messageRequest = (GroupMessageRequest) request;
        log.info(String.format(LOG_SYNTAX, getGroupName(messageRequest.getGroup_id()), messageRequest.getGroup_id(), messageRequest.getSender().getCard(),
                messageRequest.getSender().getUser_id(), messageRequest.getMessage()));

        if (messageRequest.isAt()) {
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
                    Long msgId = messageService.sendGroupMessage(messageRequest.getGroup_id(), String.format(CQCode.AT_PATTERN, messageRequest.getSender().getUser_id()) + "\n" + message);
                }
            }
        }
    }

    private String getGroupName(Long id) {
        GroupDTO groupDTO = accountManager.getGroup(id);
        if (groupDTO == null) {
            return "未知";
        }
        return groupDTO.getName();
    }
}

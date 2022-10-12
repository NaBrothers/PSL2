package com.nabrothers.psl.core.interceptor;

import com.nabrothers.psl.core.dao.UserDAO;
import com.nabrothers.psl.core.dto.UserDTO;
import com.nabrothers.psl.sdk.context.HandlerInterceptor;
import com.nabrothers.psl.sdk.context.Session;
import com.nabrothers.psl.sdk.service.MessageService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserInterceptor extends HandlerInterceptor {

    @Resource
    private UserDAO userDAO;

    @Resource
    private MessageService messageService;

    @Override
    public boolean preHandle(Session session) {
        UserDTO userDTO = userDAO.queryByUserId(session.getSender().getId());
        if (userDTO == null) {
            userDTO = new UserDTO();
            userDTO.setUserId(session.getSender().getId());
            userDTO.setName(session.getSender().getNickname());
            userDAO.insert(userDTO);
            messageService.send(session, String.format("欢迎新玩家 [%s] 加入游戏！", session.getSender().getNickname()));
        }
        return true;
    }

    @Override
    public void postHandle(Session session) {

    }

    @Override
    public String scope() {
        return "com.nabrothers.psl.core.*";
    }
}

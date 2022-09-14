package com.nabrothers.psl.core.interceptor;

import com.nabrothers.psl.core.dao.UserDAO;
import com.nabrothers.psl.core.dto.UserDTO;
import com.nabrothers.psl.sdk.context.HandlerInterceptor;
import com.nabrothers.psl.sdk.context.Session;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserInterceptor extends HandlerInterceptor {

    @Resource
    private UserDAO userDAO;

    @Override
    public boolean preHandle(Session session) {
        UserDTO userDTO = userDAO.queryByUserId(session.getSender().getId());
        if (userDTO == null) {
            userDTO = new UserDTO();
            userDTO.setUserId(session.getSender().getId());
            userDTO.setName(session.getSender().getNickname());
            userDAO.insert(userDTO);
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

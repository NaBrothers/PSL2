package com.nabrothers.psl.core.controller;

import com.nabrothers.psl.core.dao.UserDAO;
import com.nabrothers.psl.core.dto.UserDTO;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.context.Session;
import com.nabrothers.psl.sdk.context.SessionContext;
import com.nabrothers.psl.sdk.message.TextMessage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Component
public class AccountController {

    @Resource
    private UserDAO userDAO;

    @Handler(command = "账号", info = "查看账号信息")
    public TextMessage showAccount() {
        TextMessage message = new TextMessage();
        message.setTitle("账号信息");
        Session session = SessionContext.get();
        UserDTO user = userDAO.queryByUserId(session.getSender().getId());
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%d] %s\n", user.getId(), user.getName()));
        sb.append(String.format("球币：%d", user.getMoney()));
        message.setData(sb.toString());
        return message;
    }
}

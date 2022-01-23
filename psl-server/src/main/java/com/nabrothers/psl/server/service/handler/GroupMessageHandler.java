package com.nabrothers.psl.server.service.handler;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.server.request.CQHttpRequest;
import com.nabrothers.psl.server.request.GroupMessageRequest;
import com.nabrothers.psl.server.utils.HttpUtils;
import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
public class GroupMessageHandler extends MessageHandler{
    private static final String LOG_SYNTAX = "收到群 %s(%d) 内 %s(%d) 的消息: %s";

    private static Map<Long, String> groupNames = new HashMap<>();

    @Override
    public void doHandle(CQHttpRequest request) {
        GroupMessageRequest messageRequest = (GroupMessageRequest) request;
        log.info(String.format(LOG_SYNTAX, getGroupName(messageRequest.getGroup_id()), messageRequest.getGroup_id(), messageRequest.getSender().getCard(),
                messageRequest.getSender().getUser_id(), messageRequest.getMessage()));
    }

    private String getGroupName(Long id) {
        if (groupNames.get(id) != null) {
            return groupNames.get(id);
        } else {
            String res = HttpUtils.doGet("http://127.0.0.1:5700/get_group_info?group_id=" + id);
            JSONObject groupInfo = JSONObject.parseObject(res);
            String groupName = groupInfo.getJSONObject("data").getString("group_name");
            groupNames.put(id, groupName);
            return groupName;
        }
    }
}

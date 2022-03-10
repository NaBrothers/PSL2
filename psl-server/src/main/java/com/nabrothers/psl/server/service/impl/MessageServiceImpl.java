package com.nabrothers.psl.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.server.service.MessageService;
import com.nabrothers.psl.server.utils.HttpUtils;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    @Override
    public Long sendPrivateMessage(Long id, String msg) {
        JSONObject param = new JSONObject();
        param.put("user_id", id);
        param.put("message", msg);
        JSONObject res = HttpUtils.doGet("send_private_msg", param);
        if (res == null) {
            return -1L;
        }
        return res.getLong("message_id");
    }

    @Override
    public Long sendGroupMessage(Long id, String msg) {
        JSONObject param = new JSONObject();
        param.put("group_id", id);
        param.put("message", msg);
        JSONObject res = HttpUtils.doGet("send_group_msg", param);
        if (res == null) {
            return -1L;
        }
        return res.getLong("message_id");
    }
}

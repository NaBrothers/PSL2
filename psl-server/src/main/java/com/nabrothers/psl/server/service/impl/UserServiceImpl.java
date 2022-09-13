package com.nabrothers.psl.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.sdk.dto.UserDTO;
import com.nabrothers.psl.server.service.UserService;
import com.nabrothers.psl.server.utils.HttpUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class UserServiceImpl implements UserService {
    @Override
    public UserDTO getCurrentUser() {
        UserDTO userDTO = new UserDTO();
        JSONObject res = HttpUtils.doGet("get_login_info", null);
        if (res == null) {
            return userDTO;
        }
        userDTO.setId(res.getLong("user_id"));
        userDTO.setNickname(res.getString("nickname"));
        return userDTO;
    }
}

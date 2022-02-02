package com.nabrothers.psl.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.server.dto.GroupDTO;
import com.nabrothers.psl.server.service.GroupService;
import com.nabrothers.psl.server.utils.HttpUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class GroupServiceImpl implements GroupService {
    @Override
    public GroupDTO getGroupById(Long id) {
        GroupDTO groupDTO = new GroupDTO();
        JSONObject req = new JSONObject();
        req.put("group_id", id);
        JSONObject res = HttpUtils.doGet("get_group_info", req);
        if (res == null) {
            return groupDTO;
        }
        groupDTO.setId(res.getLong("group_id"));
        groupDTO.setLevel(res.getInteger("group_level"));
        groupDTO.setCreateTime(res.getLong("group_create_time"));
        groupDTO.setMemo(res.getString("group_memo"));
        groupDTO.setName(res.getString("group_name"));
        groupDTO.setMemberCount(res.getLong("member_count"));
        groupDTO.setMaxMemberCount(res.getLong("max_member_count"));
        return groupDTO;
    }
}

package com.nabrothers.psl.server.service;

import com.nabrothers.psl.sdk.dto.GroupDTO;

import java.util.List;

public interface GroupService {
    List<GroupDTO> getGroupList();

    GroupDTO getGroupById(Long id);
}

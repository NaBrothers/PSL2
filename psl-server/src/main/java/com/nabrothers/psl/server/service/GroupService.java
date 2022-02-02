package com.nabrothers.psl.server.service;

import com.nabrothers.psl.server.dto.GroupDTO;

public interface GroupService {
    GroupDTO getGroupById(Long id);
}

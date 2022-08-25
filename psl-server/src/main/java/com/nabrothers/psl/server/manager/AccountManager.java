package com.nabrothers.psl.server.manager;

import com.nabrothers.psl.server.dto.GroupDTO;
import com.nabrothers.psl.server.dto.UserDTO;
import com.nabrothers.psl.server.service.GroupService;
import com.nabrothers.psl.server.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Log4j2
public class AccountManager {
    @Resource
    private UserService userService;

    @Resource
    private GroupService groupService;

    private UserDTO currentUser;

    private Map<Long, GroupDTO> groups = new HashMap<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    private synchronized void refresh() {
        currentUser = userService.getCurrentUser();
        groups = groupService.getGroupList().stream().collect(Collectors.toMap(GroupDTO::getId, groupDTO -> groupDTO));
    }

    public UserDTO getCurrentUser() {
        return currentUser;
    }

    @Nullable
    public GroupDTO getGroup(Long id) {
        return groups.get(id);
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void scheduledRefresh() {
        refresh();
    }
}

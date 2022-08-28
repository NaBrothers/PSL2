package com.nabrothers.psl.server.context;

import com.nabrothers.psl.server.dto.GroupDTO;
import com.nabrothers.psl.server.dto.UserDTO;
import com.nabrothers.psl.server.request.enums.MessageType;
import lombok.Data;

@Data
public class Session {
    private UserDTO sender;
    private UserDTO self;
    private String message;
    private MessageType messageType;
    private GroupDTO group;
}

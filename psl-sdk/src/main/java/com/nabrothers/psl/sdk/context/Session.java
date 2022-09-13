package com.nabrothers.psl.sdk.context;


import com.nabrothers.psl.sdk.dto.GroupDTO;
import com.nabrothers.psl.sdk.dto.UserDTO;
import com.nabrothers.psl.sdk.enums.MessageType;
import lombok.Data;

@Data
public class Session {
    private UserDTO sender;
    private UserDTO self;
    private String message;
    private MessageType messageType;
    private GroupDTO group;
}

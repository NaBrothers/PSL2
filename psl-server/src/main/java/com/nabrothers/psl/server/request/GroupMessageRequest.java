package com.nabrothers.psl.server.request;

import lombok.Data;

@Data
public class GroupMessageRequest extends MessageRequest {
    private Long group_id;
    private Long message_seq;
    private boolean isAt;
}

package com.nabrothers.psl.server.request;

import lombok.Data;

@Data
public class PrivateMessageRequest extends MessageRequest {
    private Long target_id;
}

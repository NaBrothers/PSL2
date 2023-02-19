package com.nabrothers.psl.server.request;

import com.nabrothers.psl.server.request.inner.MessageSender;
import lombok.Data;

@Data
public class MessageRequest extends CQHttpRequest {
    private String raw_message;
    private Long message_id;
    private String message_type;
    private String message;
    private MessageSender sender;
    private String sub_type;
    private Long user_id;
    private Long font;
    private boolean isAt;
}

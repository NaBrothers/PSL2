package com.nabrothers.psl.server.service;

import com.nabrothers.psl.sdk.message.Message;

public interface DefaultReplyService {
    Message getReply(String message);
}

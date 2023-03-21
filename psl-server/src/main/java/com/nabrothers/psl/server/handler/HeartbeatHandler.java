package com.nabrothers.psl.server.handler;

import com.nabrothers.psl.sdk.message.Message;
import com.nabrothers.psl.server.request.CQHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class HeartbeatHandler implements RequestHandler{
    @Override
    public Message doHandle(CQHttpRequest request) {
        return null;
    }
}

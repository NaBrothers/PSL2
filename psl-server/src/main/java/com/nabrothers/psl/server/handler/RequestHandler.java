package com.nabrothers.psl.server.handler;

import com.nabrothers.psl.sdk.message.Message;
import com.nabrothers.psl.server.request.CQHttpRequest;

public interface RequestHandler {
    Message doHandle(CQHttpRequest request);
}

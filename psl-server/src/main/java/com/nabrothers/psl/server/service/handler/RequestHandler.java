package com.nabrothers.psl.server.service.handler;

import com.nabrothers.psl.server.request.CQHttpRequest;

public interface RequestHandler {
    void doHandle(CQHttpRequest request);
}

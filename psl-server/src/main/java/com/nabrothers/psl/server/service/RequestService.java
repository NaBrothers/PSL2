package com.nabrothers.psl.server.service;

import javax.servlet.http.HttpServletRequest;

public interface RequestService {
    void handleRequest(HttpServletRequest request);
}

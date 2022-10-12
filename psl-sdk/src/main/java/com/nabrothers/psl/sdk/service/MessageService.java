package com.nabrothers.psl.sdk.service;

import com.nabrothers.psl.sdk.context.Session;

public interface MessageService {
    Long sendPrivateMessage(Long id, String msg);

    Long sendGroupMessage(Long id, String msg);

    Long send(Session session, String message);
}

package com.nabrothers.psl.sdk.service;

public interface MessageService {
    Long sendPrivateMessage(Long id, String msg);

    Long sendGroupMessage(Long id, String msg);
}

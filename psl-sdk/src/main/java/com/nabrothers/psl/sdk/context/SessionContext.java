package com.nabrothers.psl.sdk.context;

import com.nabrothers.psl.sdk.utils.LRUCache;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.Map;

@Log4j2
public class SessionContext {

    private static ThreadLocal<Session> context = new ThreadLocal<>();

    private static Map<Long, Session> sessionRecord = Collections.synchronizedMap(LRUCache.createDefault(256));

    public static void add(Session session) {
        Session current = context.get();
        if (current == null) {
            context.set(session);
        } else {
            log.warn("当前会话已经存在，使用旧会话");
        }
    }

    public static Session get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }

    public static void record(Session session) {
        if (session.getMessageId() != null) {
            sessionRecord.put(session.getMessageId(), session);
        }
    }

    public static Session get(Long msgId) {
        if (msgId == null) {
            return null;
        }
        return sessionRecord.get(msgId);
    }
}

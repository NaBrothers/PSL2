package com.nabrothers.psl.sdk.context;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SessionContext {

    private static ThreadLocal<Session> context = new ThreadLocal<>();

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
}

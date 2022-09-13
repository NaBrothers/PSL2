package com.nabrothers.psl.sdk.context;

public abstract class HandlerInterceptor {
    public boolean preHandle(Session session) {
        return true;
    }

    public void postHandle(Session session) {

    }

    public String scope() {
        return ".*";
    }
}

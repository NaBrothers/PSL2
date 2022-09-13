package com.nabrothers.psl.core.interceptor;

import com.nabrothers.psl.sdk.context.HandlerInterceptor;
import com.nabrothers.psl.sdk.context.Session;

public class UserInterceptor extends HandlerInterceptor {
    @Override
    public boolean preHandle(Session session) {
        System.out.println("UserInterceptor begin");
        return true;
    }

    @Override
    public void postHandle(Session session) {
        System.out.println("UserInterceptor finish");
    }

    @Override
    public String scope() {
        return "com.nabrothers.psl.core.*";
    }
}

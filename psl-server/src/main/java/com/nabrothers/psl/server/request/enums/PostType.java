package com.nabrothers.psl.server.request.enums;

public enum PostType {
    META_EVENT("meta_event"), MESSAGE("message");

    private String name;

    PostType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PostType getByName(String name) {
        for (PostType postType : PostType.values()) {
            if (postType.getName().equals(name)) {
                return postType;
            }
        }
        return null;
    }
}
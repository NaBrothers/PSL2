package com.nabrothers.psl.server.request.enums;

public enum MessageType {
    MESSAGE_PRIVATE("private"), MESSAGE_GROUP("group");

    private String name;

    MessageType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static MessageType getByName(String name) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.getName().equals(name)) {
                return messageType;
            }
        }
        return null;
    }
}
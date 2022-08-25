package com.nabrothers.psl.server.handler;

public enum EventType {
    HEARTBEAT(0), PRIVATE_MESSAGE(1), GROUP_MESSAGE(2);

    private Integer code;

    EventType(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static EventType getByCode(Integer code) {
        for (EventType eventType : EventType.values()) {
            if (eventType.getCode().equals(code)) {
                return eventType;
            }
        }
        return null;
    }
}

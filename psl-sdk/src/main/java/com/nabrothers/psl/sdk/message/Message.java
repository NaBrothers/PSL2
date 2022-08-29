package com.nabrothers.psl.sdk.message;

import lombok.Data;

@Data
public abstract class Message {
    private boolean supportImageMode = true;
    private boolean supportAt = true;

    abstract public String getMessage();
    abstract public String getRawMessage();
}

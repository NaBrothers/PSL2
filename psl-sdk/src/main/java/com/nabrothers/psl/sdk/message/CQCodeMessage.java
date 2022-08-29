package com.nabrothers.psl.sdk.message;

import lombok.Data;

@Data
public abstract class CQCodeMessage {
    abstract public String getCode();
}

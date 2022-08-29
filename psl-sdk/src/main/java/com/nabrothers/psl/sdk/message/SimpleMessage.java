package com.nabrothers.psl.sdk.message;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class SimpleMessage extends Message {
    public SimpleMessage() {

    }

    public SimpleMessage(String data) {
        this.data = data;
    }

    private String data = "";

    @Override
    public String getMessage() {
        return StringUtils.stripEnd(data, "\n");
    }

    @Override
    public String getRawMessage() {
        return StringUtils.stripEnd(data, "\n");
    }
}

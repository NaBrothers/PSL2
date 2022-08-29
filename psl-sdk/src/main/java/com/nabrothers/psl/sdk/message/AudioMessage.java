package com.nabrothers.psl.sdk.message;

import lombok.Data;

@Data
public class AudioMessage extends Message implements CQCodeMessage {
    private String text = "";

    {
        this.setSupportAt(false);
    }

    @Override
    public String getMessage() {
        return String.format(CQCode.AUDIO_PATTERN, text);
    }

    @Override
    public String getRawMessage() {
        return text;
    }
}

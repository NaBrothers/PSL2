package com.nabrothers.psl.sdk.message;

import lombok.Data;

@Data
public class VideoMessage extends CQCodeMessage{
    String url = "";

    @Override
    public String getCode() {
        return String.format(CQCode.VIDEO_PATTERN, url);
    }

    @Override
    public String toString() {
        return getCode();
    }

}

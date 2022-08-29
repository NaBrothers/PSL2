package com.nabrothers.psl.sdk.message;

import lombok.Data;

@Data
public class VideoMessage extends Message implements CQCodeMessage{
    private String url = "";

    {
        this.setSupportAt(false);
    }

    @Override
    public String getMessage() {
        return String.format(CQCode.VIDEO_PATTERN, url);
    }

    @Override
    public String getRawMessage() {
        return "视频无法显示";
    }
}

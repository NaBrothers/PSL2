package com.nabrothers.psl.sdk.message;

import lombok.Data;

@Data
public class ImageMessage extends Message implements CQCodeMessage{
    private String url = "";

    @Override
    public String getMessage() {
        return String.format(CQCode.IMAGE_PATTERN, url);
    }

    @Override
    public String getRawMessage() {
        return "图片无法显示";
    }
}

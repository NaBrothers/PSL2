package com.nabrothers.psl.sdk.message;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ImageMessage extends Message implements CQCodeMessage{
    private String url = "";

    private String info = "";

    @Override
    public String getMessage() {
        String result = String.format(CQCode.IMAGE_PATTERN, url);
        if (StringUtils.isNotEmpty(info)) {
            result = info + "\n" + result;
        }
        return result;
    }

    @Override
    public String getRawMessage() {
        String result = url;
        if (StringUtils.isNotEmpty(info)) {
            result = info + "\n" + result;
        }
        return result;
    }
}

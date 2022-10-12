package com.nabrothers.psl.sdk.message;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class TextMessage extends Message {
    private String title = "";
    private String header = "";
    private String data = "";
    private String footer = "";
    private boolean help = true;

    public TextMessage() {

    }

    public TextMessage(String data) {
        this.data = data;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(title)) {
            sb.append(String.format("===== %s =====\n", title));
        }
        if (StringUtils.isNotEmpty(header)) {
            sb.append("--------------------\n");
            sb.append(StringUtils.stripEnd(header, "\n") + "\n");
            sb.append("--------------------\n");
        }

        sb.append(StringUtils.stripEnd(data, "\n") + "\n");

        if (StringUtils.isNotEmpty(footer)) {
            sb.append("--------------------\n");
            sb.append(StringUtils.stripEnd(footer, "\n") + "\n");
            sb.append("--------------------\n");
        }

        return StringUtils.stripEnd(sb.toString(), "\n");
    }

    @Override
    public String getRawMessage() {
        return getMessage();
    }
}

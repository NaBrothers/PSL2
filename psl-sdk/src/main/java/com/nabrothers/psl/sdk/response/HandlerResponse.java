package com.nabrothers.psl.sdk.response;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class HandlerResponse {
    private String title = "";
    private String header = "";
    private String message = "";
    private String footer = "";
    private boolean help = true;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(title)) {
            sb.append(String.format("===== %s =====\n", title));
        }
        if (StringUtils.isNotEmpty(header)) {
            sb.append("--------------------\n");
            sb.append(header + "\n");
            sb.append("--------------------\n");
        }

        sb.append(StringUtils.stripEnd(message, "\n") + "\n");

        if (StringUtils.isNotEmpty(footer)) {
            sb.append("--------------------\n");
            sb.append(footer + "\n");
            sb.append("--------------------\n");
        }

        return sb.toString();
    }
}

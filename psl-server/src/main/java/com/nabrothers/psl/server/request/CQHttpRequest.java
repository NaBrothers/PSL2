package com.nabrothers.psl.server.request;

import lombok.Data;

@Data
public abstract class CQHttpRequest {
    private String post_type;
    private Long self_id;
    private Long time;
}

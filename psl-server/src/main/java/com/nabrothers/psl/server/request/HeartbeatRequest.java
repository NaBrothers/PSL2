package com.nabrothers.psl.server.request;

import com.nabrothers.psl.server.request.inner.HeartbeatStatus;
import lombok.Data;

@Data
public class HeartbeatRequest extends CQHttpRequest {
    private Long interval;
    private String meta_event_type;
    private HeartbeatStatus status;
}

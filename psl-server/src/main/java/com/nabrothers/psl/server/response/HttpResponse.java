package com.nabrothers.psl.server.response;

import lombok.Data;

@Data
public class HttpResponse {

    public HttpResponse(String status) {
        this.status = status;
    }

    public HttpResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    private String status;
    private String message;
}

package com.nabrothers.psl.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.nabrothers.psl.server.response.HttpResponse;

@Controller
public class HealthController {
    @GetMapping(value = "/healthCheck")
    @ResponseBody
    public HttpResponse healthCheck() {
        return new HttpResponse("ok");
    }
}
package com.nabrothers.psl.server.controller;

import com.nabrothers.psl.server.response.HttpResponse;
import com.nabrothers.psl.server.service.RequestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Controller
public class CQHttpController {
    @Resource
    private RequestService requestService;

    @PostMapping
    @ResponseBody
    public HttpResponse recvMsg(HttpServletRequest request) {
        try {
            requestService.handleRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            return new HttpResponse("error", e.getMessage());
        }
        return new HttpResponse("ok");
    }
}

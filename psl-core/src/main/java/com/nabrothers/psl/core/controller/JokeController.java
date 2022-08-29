package com.nabrothers.psl.core.controller;

import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import org.springframework.stereotype.Controller;

@Controller
public class JokeController {
    @Handler(command = "讲个笑话", info = "18+笑话，未成年人禁入")
    public String getJoke() {
        return HttpUtils.doGet("https://api.oddfar.com/yl/q.php?c=1004").replaceAll("<br>", "\n");
    }
}

package com.nabrothers.psl.server.controller;

import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.server.context.HandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@Handler(command = "帮助")
public class HelpController {
    private HandlerContext context = HandlerContext.getInstance();

    @Handler(info = "查看支持的指令")
    public String help() {
        Map<String, HandlerContext.Node> commands = context.getHead().getChildren();
        StringBuilder sb = new StringBuilder("支持的指令:");
        for (Map.Entry<String, HandlerContext.Node> command : commands.entrySet()) {
            if (command.getValue().getHandler().isHidden()) {
                continue;
            }
            sb.append("\n");
            sb.append(String.format("[%s] %s",
                    command.getKey(),
                    StringUtils.isEmpty(command.getValue().getHandler().getInfo()) ? "暂无" : command.getValue().getHandler().getInfo())
            );
        }
        return sb.toString();
    }
}

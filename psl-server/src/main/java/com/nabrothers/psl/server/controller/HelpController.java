package com.nabrothers.psl.server.controller;

import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.server.context.HandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
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
            HandlerContext.HandlerMethod handlerMethod = command.getValue().getDefaultHandler();
            assert handlerMethod != null;
            if (handlerMethod.isHidden()) {
                continue;
            }
            sb.append("\n");
            sb.append(String.format("[%s] %s",
                    command.getKey(),
                    StringUtils.isEmpty(handlerMethod.getInfo()) ? "暂无说明" : handlerMethod.getInfo())
            );
        }
        return sb.toString();
    }

    @Handler(info = "查看指令详情")
    public String help(String cmd) {
       HandlerContext.Node node = context.getHead().getChild(cmd);
       if (node == null) {
           return String.format("找不到指令 [%s]", cmd);
       }
       List<HandlerContext.Node> children = node.getAllMethodChildren();
       if (!node.getHandlers().isEmpty()) {
           children.add(node);
       }

       children.sort((a, b) -> {
           if (a.getCommand().length() == b.getCommand().length()) {
               return a.getCommand().compareTo(b.getCommand());
           } else if (a.getCommand().length() < b.getCommand().length()){
               return -1;
           } else {
               return 1;
           }
       });

       StringBuilder sb = new StringBuilder();
       for (HandlerContext.Node child : children) {
           for (HandlerContext.HandlerMethod method : child.getHandlers()) {
               sb.append(String.format("[%s] ", child.getCommand()));
               int i = 1;
               for (Class param : method.getMethod().getParameterTypes()) {
                   sb.append(String.format("(参数%d) ", i++));
               }
               sb.append("\n→");
               sb.append(StringUtils.isEmpty(method.getInfo()) ? "暂无说明" : method.getInfo());
               sb.append("\n");
           }
       }
       return sb.toString();
    }


    @Handler(command = "2")
    public String help2() {
        return "2";
    }

    @Handler(command = "1 2")
    public String help3(int a) {
        return "1 2";
    }

    @Handler(command = "1 1")
    public String help1(String a, int b) {
        return "1 1";
    }
}

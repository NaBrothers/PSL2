package com.nabrothers.psl.server.controller;

import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Param;
import com.nabrothers.psl.sdk.enums.TriggerType;
import com.nabrothers.psl.sdk.message.TextMessage;
import com.nabrothers.psl.server.context.HandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Handler(command = "帮助")
public class HelpController {
    private HandlerContext context = HandlerContext.getInstance();

    @Handler(info = "查看支持的指令")
    public TextMessage help() {
        TextMessage response = new TextMessage();
        response.setTitle("帮助菜单");
        Map<String, HandlerContext.Node> commands = context.getHead().getChildren();
        List<String> keys = new ArrayList<>(commands.keySet());

        keys.sort((a, b) -> {
            if (a.length() == b.length()) {
                return a.compareTo(b);
            } else if (a.length() < b.length()){
                return -1;
            } else {
                return 1;
            }
        });

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            HandlerContext.HandlerMethod handlerMethod = commands.get(key).getDefaultHandler();
            assert handlerMethod != null;
            if (handlerMethod.isHidden()) {
                continue;
            }

            sb.append(String.format("[%s] %s", key, StringUtils.isEmpty(handlerMethod.getInfo()) ? "暂无说明" : handlerMethod.getInfo()));
            sb.append("\n");
        }
        response.setData(sb.toString());
        return response;
    }

    @Handler(info = "查看指令详情")
    public TextMessage help(@Param("指令名") String cmd) {
       TextMessage response = new TextMessage();
       response.setTitle("指令详情");
       HandlerContext.Node node = context.getHead().getChild(cmd);
       if (node == null) {
           response.setData(String.format("找不到指令 [%s]", cmd));
           return response;
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
               for (Parameter param : method.getMethod().getParameters()) {
                   Param annotation = param.getAnnotation(Param.class);
                   if (annotation == null) {
                       sb.append(String.format("(参数%d) ", i));
                   } else {
                       sb.append(String.format("(%s) ", annotation.value()));
                   }
                   i++;
               }
               sb.append("\n  →");
               sb.append(StringUtils.isEmpty(method.getInfo()) ? "暂无说明" : method.getInfo());
               sb.append("\n");
           }
       }
       response.setData(sb.toString());
       return response;
    }
}

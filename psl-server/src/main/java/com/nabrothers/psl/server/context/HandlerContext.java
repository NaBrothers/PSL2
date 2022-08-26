package com.nabrothers.psl.server.context;

import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.server.utils.ApplicationContextUtils;
import com.nabrothers.psl.server.utils.CommonUtils;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Log4j2
public class HandlerContext {
    private static HandlerContext instance = new HandlerContext();

    private class Node {
        private Node parent;
        private Map<String, Node> children = new HashMap<>();
        private HandlerMethod handler;
        private String command;
    }

    private class HandlerMethod {
        private Method method;
        private String info;
        private String command;
    }

    private Node head = new Node();

    private Set<String> packages = new HashSet<>();

    public static HandlerContext getInstance() {
        return instance;
    }

    public void load(String name) {
        if (packages.contains(name)) {
            return;
        }
        try {
            Reflections reflections = new Reflections(new ConfigurationBuilder().
                    setUrls(ClasspathHelper.forPackage(name)).
                    setScanners(new MethodAnnotationsScanner()));
            if (reflections.getConfiguration().getUrls().isEmpty()) {
                throw new ClassNotFoundException(name);
            }
            Set<Method> methods = reflections.getMethodsAnnotatedWith(Handler.class);
            for (Method method : methods) {
                Handler annotation = method.getDeclaredAnnotation(Handler.class);
                String cmd = annotation.command();
                Class clazz = method.getDeclaringClass();
                if (clazz.isAnnotationPresent(Handler.class)) {
                    Handler classAnnotation = (Handler) clazz.getDeclaredAnnotation(Handler.class);
                    cmd = classAnnotation.command() + " " + cmd;
                }
                List<String> commands = Arrays.asList(cmd.split(" "));
                _parse(head, commands, method);
            }
            packages.add(name);
            log.info("插件包加载成功：" + name + ", 共加载 " + methods.size() + " 个命令");
        } catch (Exception e) {
            log.error("插件包加载失败：" + name, e);
            return;
        }
    }

    private void _parse(Node node, List<String> commands, Method method) {
        if (commands.isEmpty()) {
            if (node.handler != null) {
                String signature1 = node.handler.method.getDeclaringClass().getName() + ":" + node.handler.method.getName();
                String signature2 = method.getDeclaringClass().getName() + ":" + method.getName();
                if (!signature1.equals(signature2)) {
                    throw new RuntimeException("指令冲突: " + node.command + ", " + signature1 + " & " + signature2);
                }
                return;
            }
            node.handler = new HandlerMethod();
            Handler annotation = method.getDeclaredAnnotation(Handler.class);
            node.handler.method = method;
            node.handler.command = annotation.command();
            node.handler.info = annotation.info();
            return;
        }

        String nextCommand = commands.get(0);
        Node child;
        if (node.children.containsKey(nextCommand)) {
            child = node.children.get(nextCommand);
        } else {
            child = new Node();
            child.parent = node;
            child.command = node == head ? nextCommand : node.command + " " + nextCommand;
            node.children.put(nextCommand, child);
        }

        _parse(child, CommonUtils.subList(commands, 1, commands.size()), method);
    }

    public String handle(String command) {
        List<String> commands = Arrays.asList(command.split(" "));
        return _handle(head, commands);
    }

    private String _handle(Node node, List<String> commands) {
        if (commands.isEmpty()) {
            return invoke(node, commands);
        }
        String nextCommand = commands.get(0);
        if (node.children.containsKey(nextCommand)) {
            Node child = node.children.get(nextCommand);
            return _handle(child, CommonUtils.subList(commands, 1, commands.size()));
        } else {
            return invoke(node, commands);
        }
    }

    private String invoke(Node node, List<String> args) {
        if (node.handler == null) {
            throw new RuntimeException(String.format("找不到指令 [%s]\n支持的指令: %s", args.get(0), head.children.keySet()));
        }
        int paramCount = node.handler.method.getParameterCount();
        if (args.size() < paramCount) {
            throw new RuntimeException(String.format("指令 [%s] 需要 %d 个参数", node.command, paramCount));
        }
        try {
            List<String> methodArgs = new ArrayList<>();
            for (int i = 0; i < paramCount; i++) {
                if (i == paramCount - 1) {
                    methodArgs.add(String.join(" ", CommonUtils.subList(args, i, args.size())));
                } else {
                    methodArgs.add(args.get(i));
                }
            }
            Object obj;
            try {
                obj = ApplicationContextUtils.getBean(node.handler.method.getDeclaringClass());
            } catch (Exception e) {
                log.warn("找不到Bean，创建新实例: " + node.handler.method.getDeclaringClass());
                obj = node.handler.method.getDeclaringClass().newInstance();
            }
            String res = (String) node.handler.method.invoke(obj, methodArgs.toArray());
            return res;
        } catch (Exception e) {
            Throwable invocationTargetException = ((InvocationTargetException) e).getTargetException();
            throw new RuntimeException("函数调用异常:\n" +
                    invocationTargetException.getMessage() + "\n" +
                    "[异常堆栈]\n" +
                    getStackTrace(invocationTargetException));
        }
    }

    private String getStackTrace(Throwable e) {
        StackTraceElement[] stackElements = e.getStackTrace();
        StringBuilder sb = new StringBuilder();
        if (null != stackElements) {
            for (int i = 0; i < stackElements.length; i++) {
                if (i > 10) {
                    sb.append(String.format("......"));
                    break;
                }
                sb.append(stackElements[i].getClassName());
                sb.append(".").append(stackElements[i].getMethodName());
                sb.append("(").append(stackElements[i].getFileName()).append(":");
                sb.append(stackElements[i].getLineNumber()+")").append("\n");
            }
        }
        return sb.toString();
    }
}

package com.nabrothers.psl.server.context;

import com.google.common.base.Joiner;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Hidden;
import com.nabrothers.psl.sdk.context.HandlerInterceptor;
import com.nabrothers.psl.sdk.context.SessionContext;
import com.nabrothers.psl.server.config.GlobalConfig;
import com.nabrothers.psl.server.dto.Plugin;
import com.nabrothers.psl.server.service.DefaultReplyService;
import com.nabrothers.psl.server.service.impl.DefaultReplyServiceImpl;
import com.nabrothers.psl.server.utils.ApplicationContextUtils;
import com.nabrothers.psl.server.utils.CommonUtils;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class HandlerContext {
    private static HandlerContext instance = new HandlerContext();

    private static ThreadLocal<Plugin> plugin = new ThreadLocal<>();

    private static ThreadLocal<String> cmd = new ThreadLocal<>();

    public class Node {
        private Node parent;
        private Map<String, Node> children = new HashMap<>();
        private Map<Integer, HandlerMethod> handlers = new HashMap<>();
        private String command;

        public Node getParent() {
            return parent;
        }

        public Map<String, Node> getChildren() {
            return children;
        }

        public List<HandlerMethod> getHandlers() {
            return new ArrayList<>(handlers.values());
        }

        @Nullable
        public Node getChild(String cmd) {
            return children.get(cmd);
        }

        @Nullable
        public HandlerMethod getHandler(int paramCount) {
            return handlers.get(paramCount);
        }

        @Nullable
        public HandlerMethod getDefaultHandler() {
            HandlerMethod handlerMethod = handlers.get(0);
            if (handlerMethod == null) {
                handlerMethod = handlers.values().iterator().next();
            }
            return handlerMethod;
        }

        public List<Node> getAllChildren() {
            List<Node> nodes = new ArrayList<>();
            for (Node child : children.values()) {
                nodes.add(child);
                nodes.addAll(child.getAllChildren());
            }
            return nodes;
        }

        public List<Node> getAllMethodChildren() {
            return getAllChildren().stream().filter(node -> !node.handlers.isEmpty()).collect(Collectors.toList());
        }

        public String getCommand() {
            return command;
        }
    }

    public class HandlerMethod {
        private Method method;
        private String info;
        private String command;
        private boolean hidden;
        private Plugin plugin;

        public Method getMethod() {
            return method;
        }

        public String getInfo() {
            return info;
        }

        public String getCommand() {
            return command;
        }

        public boolean isHidden() {
            return hidden;
        }

        public Plugin getPlugin() {
            return plugin;
        }
    }

    private Node head = new Node();

    private Set<String> packages = new HashSet<>();

    private Map<String, List<HandlerInterceptor>> interceptors = new HashMap<>();

    public static HandlerContext getInstance() {
        return instance;
    }

    public void load(Plugin plugin) {
        String name = plugin.getPackageName();
        if (packages.contains(name)) {
            return;
        }
        this.plugin.set(plugin);
        try {
            Reflections reflections = new Reflections(new ConfigurationBuilder().
                    setUrls(ClasspathHelper.forPackage(name)).
                    setScanners(new MethodAnnotationsScanner(), new SubTypesScanner()));
            if (reflections.getConfiguration().getUrls().isEmpty()) {
                throw new ClassNotFoundException(name);
            }
            // 处理拦截器
            Set<Class<? extends HandlerInterceptor>> interceptors = reflections.getSubTypesOf(HandlerInterceptor.class);
            this.interceptors.put(name, interceptors.stream()
                    .filter(clazz -> clazz.getPackage().getName().startsWith(name))
                    .map(clazz -> {
                        try {
                            return clazz.newInstance();
                        } catch (Exception e) {
                            log.error(e);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
            );

            // 处理Handler注解
            Set<Method> methods = reflections.getMethodsAnnotatedWith(Handler.class);
            int count = 0;
            for (Method method : methods) {
                if (!method.getDeclaringClass().getPackage().getName().startsWith(name)) {
                    continue;
                }
                Handler annotation = method.getDeclaredAnnotation(Handler.class);
                String cmd = annotation.command();
                Class clazz = method.getDeclaringClass();
                if (clazz.isAnnotationPresent(Handler.class)) {
                    Handler classAnnotation = (Handler) clazz.getDeclaredAnnotation(Handler.class);
                    cmd = classAnnotation.command() + " " + cmd;
                }
                List<String> commands = Arrays.asList(cmd.split(" "));
                _parse(head, commands, method);
                count++;
            }
            packages.add(name);
            log.info("插件包加载成功：" + name + ", 共加载 " + count + " 个命令");
        } catch (Exception e) {
            log.error("插件包加载失败：" + name, e);
        }
        this.plugin.remove();
    }

    private void _parse(Node node, List<String> commands, Method method) {
        if (commands.isEmpty()) {
            if (node.handlers.get(method.getParameterCount()) != null) {
                Method exist = node.handlers.get(method.getParameterCount()).getMethod();
                String signature1 = exist.getDeclaringClass().getName() + ":" + exist.getName();
                String signature2 = method.getDeclaringClass().getName() + ":" + method.getName();
                throw new RuntimeException("指令冲突: " + node.command + ", " + signature1 + " & " + signature2);
            }
            HandlerMethod handlerMethod = new HandlerMethod();
            node.handlers.put(method.getParameterCount(), handlerMethod);
            Handler annotation = method.getDeclaredAnnotation(Handler.class);
            handlerMethod.method = method;
            handlerMethod.command = annotation.command();
            handlerMethod.info = annotation.info();
            handlerMethod.hidden = method.isAnnotationPresent(Hidden.class) || method.getDeclaringClass().isAnnotationPresent(Hidden.class);
            handlerMethod.plugin = plugin.get();
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

    public Object handle(String command) {
        cmd.set(command);
        List<String> commands = Arrays.asList(command.split(" "));
        Object result = _handle(head, commands);
        cmd.remove();
        return result;
    }

    private Object _handle(Node node, List<String> commands) {
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

    private Object _invoke(HandlerMethod handlerMethod, List<String> args) {
        List<HandlerInterceptor> availableInterceptors = interceptors.get(handlerMethod.getPlugin().getPackageName())
                .stream().filter(interceptor -> handlerMethod.getMethod().getDeclaringClass().getPackage().getName().matches(interceptor.scope()))
                .collect(Collectors.toList());

        boolean passAll = true;
        for (HandlerInterceptor interceptor : availableInterceptors) {

            try {
                boolean result = interceptor.preHandle(SessionContext.get());
                if (result == false) {
                    passAll = false;
                    log.warn("会话未通过拦截器 {}: {}", interceptor.getClass().getName(), SessionContext.get());
                }
            } catch (Exception e) {
                throw new RuntimeException("拦截器异常:\n" +
                        e.getMessage() + "\n" +
                        "[异常堆栈]\n" +
                        getStackTrace(e));
            }
        }

        Object result = null;
        if (passAll) {
            try {
                Object obj;
                try {
                    obj = ApplicationContextUtils.getBean(handlerMethod.method.getDeclaringClass());
                } catch (Exception e) {
                    log.warn("找不到Bean，创建新实例: " + handlerMethod.method.getDeclaringClass());
                    obj = handlerMethod.method.getDeclaringClass().newInstance();
                }
                result =  handlerMethod.method.invoke(obj, args.toArray());
            } catch (InvocationTargetException e) {
                Throwable invocationTargetException = e.getTargetException();
                throw new RuntimeException("函数调用异常:\n" +
                        invocationTargetException.getMessage() + "\n" +
                        "[异常堆栈]\n" +
                        getStackTrace(invocationTargetException));
            } catch (Exception e) {
                throw new RuntimeException("其他异常：\n" + e.getMessage());
            }
        }

        for (HandlerInterceptor interceptor : availableInterceptors) {
            if (!handlerMethod.getMethod().getDeclaringClass().getPackage().getName().matches(interceptor.scope())) {
                continue;
            }
            try {
                interceptor.postHandle(SessionContext.get());
            } catch (Exception e) {
                throw new RuntimeException("拦截器异常:\n" +
                        e.getMessage() + "\n" +
                        "[异常堆栈]\n" +
                        getStackTrace(e));
            }
        }

        return result;
    }

    private Object invoke(Node node, List<String> args) {
        if (node.handlers.isEmpty()) {
            if (GlobalConfig.ENABLE_DEFAULT_REPLY) {
                DefaultReplyService defaultReplyService = ApplicationContextUtils.getBean(DefaultReplyServiceImpl.class);
                return defaultReplyService.getReply(cmd.get());
            } else {
                throw new RuntimeException(String.format("找不到指令 [%s]\n请输入 [帮助] 查看支持的指令", args.get(0)));
            }
        }
        HandlerMethod handlerMethod = node.handlers.get(args.size());
        if (handlerMethod == null) {
            throw new RuntimeException(String.format("指令 [%s] 需要 (%s) 个参数\n请输入 [帮助 %s] 查看指令格式",
                    node.command,
                    Joiner.on("/").join(node.handlers.keySet()),
                    node.command.split(" ")[0]));
        }
        return _invoke(handlerMethod, args);
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

    public Node getHead() {
        return head;
    }
}

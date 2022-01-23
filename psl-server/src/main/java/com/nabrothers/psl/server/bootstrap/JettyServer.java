package com.nabrothers.psl.server.bootstrap;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.nio.file.Path;

public class JettyServer {
    private static final int HTTP_PORT = 5701;

    public static final String CONTEXT = "/";

    private static final String DEFAULT_WEBAPP_PATH = "psl-server/src/main/webapp";

    private static final String WEB_XML_PATH = "psl-server/src/main/webapp/WEB-INF/web.xml";

    public static void main(String[] args) throws Exception {
        Server server = new JettyServer().createServer();
        server.start();
        server.join();
    }

    private Server createServer() {
        // Create a Server instance.
        Server server = new Server();

        // Create and configure a ThreadPool.
        QueuedThreadPool queuedThreadPool = new QueuedThreadPool();
        queuedThreadPool.setName("queuedTreadPool");
        queuedThreadPool.setMinThreads(10);
        queuedThreadPool.setMaxThreads(200);
        server.setThreadPool(queuedThreadPool);

        // Create a ServerConnector to accept connections from clients.
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(HTTP_PORT);
        connector.setAcceptors(4);// 同时监听read事件的线程数
        connector.setMaxBuffers(2048);
        connector.setMaxIdleTime(10000);
        server.addConnector(connector);
        server.addConnector(connector);

        Path defaultPath = new File(DEFAULT_WEBAPP_PATH).toPath();
        Path webXmlPath = new File(WEB_XML_PATH).toPath();

        WebAppContext webContext = new WebAppContext(DEFAULT_WEBAPP_PATH, CONTEXT);
        webContext.setResourceBase(defaultPath.toAbsolutePath().toString());
        webContext.setDescriptor(webXmlPath.toAbsolutePath().toString());
        webContext.setClassLoader(Thread.currentThread().getContextClassLoader());
        server.setHandler(webContext);

        return server;
    }
}

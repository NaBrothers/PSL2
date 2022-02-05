package com.nabrothers.psl.server.bootstrap;

import com.nabrothers.psl.server.manager.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ServerMainApp implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    PluginManager pluginManager;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //过滤DispatcherServlet context，只有root application context才进行后续初始化
        if(event.getApplicationContext().getParent() == null){
            pluginManager.init();
        }
    }
}

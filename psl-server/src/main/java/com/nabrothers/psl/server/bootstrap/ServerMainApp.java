package com.nabrothers.psl.server.bootstrap;

import com.nabrothers.psl.server.manager.ChannelManager;
import com.nabrothers.psl.server.manager.PluginManager;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ServerMainApp implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    PluginManager pluginManager;

    @Resource
    ChannelManager channelManager;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //过滤DispatcherServlet context，只有root application context才进行后续初始化
        if(event.getApplicationContext().getParent() == null){
            pluginManager.init();
            channelManager.init();
        }
    }
}

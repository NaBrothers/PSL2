package com.nabrothers.psl.server.manager;

import com.nabrothers.psl.server.config.GlobalConfig;
import io.github.kloping.qqbot.Starter;
import io.github.kloping.qqbot.api.Intents;
import io.github.kloping.qqbot.api.message.MessageChannelReceiveEvent;
import io.github.kloping.qqbot.impl.ListenerHost;
import org.springframework.stereotype.Component;

@Component
public class ChannelManager {
    public void init() {
        Starter starter=new Starter(GlobalConfig.BOT_APPID,GlobalConfig.BOT_TOKEN);
        starter.getConfig().setCode(Intents.PRIVATE_INTENTS.getCode());
        starter.registerListenerHost(new ListenerHost(){
            @EventReceiver
            public void onEvent(MessageChannelReceiveEvent event){
                event.send("测试");
            }
        });
        starter.run();
    }
}

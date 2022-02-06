package com.nabrothers.psl.server.manager;

import com.nabrothers.psl.server.context.HandlerContext;
import com.nabrothers.psl.server.dto.Plugin;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
@Log4j2
public class PluginManager {
    private HandlerContext context = HandlerContext.getInstance();

    private List<Plugin> plugins = new ArrayList<>();

    public void init() {
        try {
            File pluginDir = new File("./psl-server/target/plugins");
            File[] properties = pluginDir.listFiles(pathname -> pathname.isFile() && pathname.getName().endsWith(".plugin.properties"));
            if (properties == null) {
                log.warn("找不到plugins文件夹: " + pluginDir.getCanonicalPath());
                return;
            }
            for (File property : properties) {
                Properties p = new Properties();
                p.load(new FileInputStream(property));

                Plugin plugin = new Plugin();
                plugin.setName(p.getProperty("plugin.name"));
                plugin.setInfo(p.getProperty("plugin.info"));
                plugin.setAuthor(p.getProperty("plugin.author"));
                plugin.setPackageName(p.getProperty("plugin.package"));
                plugin.setPath(property.getCanonicalPath());
                log.info("加载插件\n" + plugin);

                context.load(plugin.getPackageName());

                plugins.add(plugin);
            }
        } catch (Exception e) {
            log.error("插件路径解析失败", e);
        }
    }
}

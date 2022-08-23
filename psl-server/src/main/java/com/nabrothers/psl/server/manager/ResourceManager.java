package com.nabrothers.psl.server.manager;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceManager {
    public static void main(String[] args) {
        try {
            Files.createDirectories(Paths.get("./psl-server/target/plugins"));

            File dir = new File("./");
            File[] modules = dir.listFiles(module -> module.isDirectory());
            for (File module : modules) {
                File properties = new File(module.getAbsolutePath() + "/target/classes/plugin.properties");
                if (properties.exists()) {
                    String name = module.getName();
                    System.out.println(String.format("[%s] 加载模块资源", name));

                    String cmd1 = String.format("cp -r %s %s", module.getAbsolutePath() + "/target/classes/", "./psl-server/target/classes/");
                    System.out.println(String.format("[%s] 复制classes：%s", name, cmd1));
                    Process process1 = Runtime.getRuntime().exec(cmd1);
                    waitForReturn(process1);

                    String cmd2 = String.format("mv %s %s", "./psl-server/target/classes/plugin.properties", "./psl-server/target/plugins/" + name+ ".plugin.properties");
                    System.out.println(String.format("[%s] 复制plugin.properties: %s", name, cmd2));
                    Process process2 = Runtime.getRuntime().exec(cmd2);
                    waitForReturn(process2);

                    System.out.println(String.format("[%s] 模块资源加载成功", name));
                }
            }
        } catch (Exception e) {
            System.out.println("模块资源加载失败");
            e.printStackTrace();
        }
    }

    private static void waitForReturn(Process process) throws Exception {
        int ret = process.waitFor();
        if (ret == 0) {
            StringBuffer result = new StringBuffer();
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            if (StringUtils.isNotEmpty(result.toString())) {
                System.out.println(result);
            }
        }
    }
}

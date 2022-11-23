package com.nabrothers.psl.core.controller;

import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.message.CQCode;
import com.nabrothers.psl.sdk.message.SimpleMessage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BController {
    @Handler(command = "看看批", info = "看看你的批")
    public SimpleMessage showB() {
        SimpleMessage message = new SimpleMessage();
        StringBuilder sb = new StringBuilder();
        int page = new Random().nextInt(50) + 1;
        try {
            String res = HttpUtils.doGet("https://xn--frx-dickintheworld-com-d678ae37jzkza746y.bd-friend.com?type=99&page=" + page);
            String regex = "\"/files/hpic/.*/>";
            Pattern compile = Pattern.compile(regex);
            Matcher m = compile.matcher(res);
            List<String> paths = new ArrayList<>();
            List<String> infos = new ArrayList<>();
            while (m.find()) {
                String path = m.group().split(" ")[0];
                path = path.substring(1, path.length() - 1);
                paths.add(path);

                String info = m.group().split(" ")[2];
                info = info.substring(6, info.length() - 1);
                infos.add(info);
            }
            if (paths.isEmpty()) {
                return message;
            }
            int index = new Random().nextInt(paths.size());
            sb.append(infos.get(index) + "\n");
            String url = paths.get(index);
            sb.append(String.format(CQCode.IMAGE_PATTERN, "https://xn--frx-dickintheworld-com-d678ae37jzkza746y.bd-friend.com/" + url));
            message.setData(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    @Handler(command = "看看鸡", info = "看看你的鸡")
    public SimpleMessage showJ() {
        SimpleMessage message = new SimpleMessage();
        StringBuilder sb = new StringBuilder();
        int page = new Random().nextInt(300) + 1;
        try {
            String res = HttpUtils.doGet("https://xn--frx-dickintheworld-com-d678ae37jzkza746y.bd-friend.com?type=1&page=" + page);
            String regex = "\"/files/hpic/.*/>";
            Pattern compile = Pattern.compile(regex);
            Matcher m = compile.matcher(res);
            List<String> paths = new ArrayList<>();
            List<String> infos = new ArrayList<>();
            while (m.find()) {
                String path = m.group().split(" ")[0];
                path = path.substring(1, path.length() - 1);
                paths.add(path);

                String info = m.group().split(" ")[2];
                info = info.substring(6, info.length() - 1);
                infos.add(info);
            }
            if (paths.isEmpty()) {
                return message;
            }
            int index = new Random().nextInt(paths.size());
            sb.append(infos.get(index) + "\n");
            String url = paths.get(index);
            sb.append(String.format(CQCode.IMAGE_PATTERN, "https://xn--frx-dickintheworld-com-d678ae37jzkza746y.bd-friend.com/" + url));
            message.setData(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }
}

package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Param;
import com.nabrothers.psl.sdk.context.Session;
import com.nabrothers.psl.sdk.context.SessionContext;
import com.nabrothers.psl.sdk.enums.MessageType;
import com.nabrothers.psl.sdk.message.TextMessage;
import com.nabrothers.psl.sdk.service.MessageService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Handler(command = "看看球")
@Log4j2
public class Zhibo8Controller {
    private static volatile boolean start = false;

    private static final Map<String, String> typeMap = new HashMap(){{
        put("football", "足球");
        put("basketball", "篮球");
        put("game", "电竞");
        put("other", "其他");
    }};

    @Resource
    private MessageService messageService;

    @Handler(info = "当前可以看的比赛")
    public TextMessage show() {
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("当前比赛");
        String url = "https://bifen4m.qiumibao.com/json/list.htm?_t=" + System.currentTimeMillis();
        String retStr = HttpUtils.doGet(url);
        JSONArray result = JSON.parseObject(retStr).getJSONArray("list");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        List<JSONObject> resultList = result.stream().map(a -> (JSONObject)a)
                .filter(a -> {
                    try {
                        return sdf.parse(a.getString("start")).getTime() > System.currentTimeMillis() - 6 * 60 * 60 * 1000;
                    } catch (ParseException e) {
                        return false;
                    }
                })
                .sorted(Comparator.comparing(a -> a.getString("start")))
                .collect(Collectors.toList());

        Map<String, List<JSONObject>> map = new HashMap<>();
        for (JSONObject jsonObject : resultList) {
            String type = typeMap.get(jsonObject.getString("type"));
            if (map.get(type) == null) {
                map.put(type, new ArrayList<>());
            }
            map.get(type).add(jsonObject);
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<JSONObject>> entry : map.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            sb.append("===== " + entry.getKey() + " ======\n");
            for (JSONObject jsonObject : entry.getValue()) {
                if (jsonObject.getString("is_prima").equals("1")) {
                    sb.append("★ ");
                }
                sb.append(String.format("[%s] %s %s %s-%s %s %s",
                        jsonObject.getString("id"),
                        jsonObject.getString("time"),
                        jsonObject.getString("home_team"),
                        jsonObject.getString("home_score"),
                        jsonObject.getString("visit_score"),
                        jsonObject.get("visit_team"),
                        jsonObject.get("period_cn")
                ));
                sb.append("\n");
            }
        }
        textMessage.setData(sb.toString());
        return textMessage;
    }

    @Handler(info = "看文字直播")
    public String live(@Param("比赛ID") String id) {
        if (start) {
            return "直播已经在进行中";
        }

        Session session = SessionContext.get();
        if (session.getMessageType() == MessageType.MESSAGE_PRIVATE) {
            return "直播只能在群里进行";
        }

        Long groupId = session.getGroup().getId();
        new Thread(() -> {
            start = true;
            try {
                String infoStr = HttpUtils.doGet(String.format("https://m.zhibo8.cc/json/match/%s.htm", id));
                if (infoStr == null) {
                    throw new RuntimeException("当前比赛不存在");
                }
                JSONObject info = JSON.parseObject(infoStr);
                String infoMsg = String.format("正在为您直播的比赛是：[%s] %s vs %s",
                        info.getJSONObject("league").getString("name_cn"),
                        info.getJSONObject("left_team").getString("name"),
                        info.getJSONObject("right_team").getString("name")
                );
                messageService.sendGroupMessage(groupId, infoMsg);

                int cur = 0;
                while (start) {
                    String res = HttpUtils.doGet(String.format("https://dingshi4m.qiumibao.com/livetext/data/cache/max_sid/%s/0.htm", id));
                    if (res == null) {
                        throw new RuntimeException("暂无直播数据");
                    }
                    int next = Integer.parseInt(res);
                    if (next > 0 && cur == 0) {
                        cur = next - 1;
                    }
                    int i = cur + 1;
                    for (; i <= next; ) {
                        String text = HttpUtils.doGet(String.format("https://dingshi4m.qiumibao.com/livetext/data/cache/livetext/%s/0/lit_page_2/%d.htm", id, (i + 1) / 2 * 2));
                        if (text == null) {
                            break;
                        }
                        JSONArray array = JSON.parseArray(text);
                        for (Object obj : array) {
                            JSONObject jsonObject = (JSONObject) obj;
                            if (Integer.parseInt(jsonObject.getString("live_sid")) < i) {
                                continue;
                            }
                            String msg = String.format("(%s-%s) [%s] %s",
                                    jsonObject.getString("home_score"),
                                    jsonObject.getString("visit_score"),
                                    jsonObject.getString("live_ptime"),
                                    jsonObject.getString("live_text"));
                            messageService.sendGroupMessage(groupId, msg);
                            i++;
                        }
                    }
                    cur = next;
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                messageService.sendGroupMessage(groupId, "直播出错：" + e.getMessage());
                log.error(e);
            } finally {
                start = false;
                messageService.sendGroupMessage(groupId, "直播已结束");
            }
        }).start();

        return "文字直播开始";
    }

    @Handler(command = "不看了", info = "不看了")
    public String over() {
        if (start) {
            start = false;
        }
        return "直播已停止";
    }
}

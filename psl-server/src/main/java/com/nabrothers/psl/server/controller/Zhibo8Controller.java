package com.nabrothers.psl.server.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Param;
import com.nabrothers.psl.sdk.message.SimpleMessage;
import com.nabrothers.psl.sdk.message.TextMessage;
import com.nabrothers.psl.server.context.Session;
import com.nabrothers.psl.server.context.SessionContext;
import com.nabrothers.psl.server.request.enums.MessageType;
import com.nabrothers.psl.server.service.MessageService;
import com.nabrothers.psl.server.utils.HttpUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Controller
@Handler(command = "看看球")
@Log4j2
public class Zhibo8Controller {
    private static volatile boolean start = false;

    @Resource
    private MessageService messageService;

    @Handler(info = "当前可以看的比赛")
    public TextMessage show() {
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("当前比赛");
        String url = "https://bifen4m.qiumibao.com/json/list.htm?_t=" + System.currentTimeMillis();
        String retStr = HttpUtils.doGet(url);
        JSONArray result = JSON.parseObject(retStr).getJSONArray("list");
        StringBuilder sb = new StringBuilder();
        for (Object obj : result) {
            JSONObject jsonObject = (JSONObject) obj;
            if (jsonObject.getString("is_prima").equals("0")) {
                continue;
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
                            String msg = String.format("[%s] (%s-%s) %s",
                                    jsonObject.getString("live_ptime"),
                                    jsonObject.getString("home_score"),
                                    jsonObject.getString("visit_score"),
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
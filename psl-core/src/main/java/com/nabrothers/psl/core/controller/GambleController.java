package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Param;
import com.nabrothers.psl.sdk.message.TextMessage;
import com.nabrothers.psl.sdk.service.MessageService;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;

@Controller
@Handler(command = "菠菜")
public class GambleController {
    @Resource
    private MessageService messageService;

    @Handler(info = "当前可以赌的比赛")
    public TextMessage gamble() {
        TextMessage message = new TextMessage();
        message.setTitle("当前比赛");
        StringBuilder sb = new StringBuilder();
        String retStr = HttpUtils.doGet("https://guess.qiumibao.com/saishi/zbbList?type=football_zc");
        JSONArray dateList = JSON.parseObject(retStr).getJSONObject("data").getJSONArray("list");
        for (Object dateObj : dateList) {
            JSONObject date = (JSONObject) dateObj;
            sb.append(String.format("===== %s =====\n", date.getString("time_title")));
            JSONArray games = date.getJSONArray("list");
            for (Object gameObj : games) {
                JSONObject game = (JSONObject) gameObj;
                sb.append(String.format("[%s] %s [%s] %s %s-%s %s %s\n",
                        game.getString("saishi_id"),
                        game.getString("stime"),
                        game.getString("league"),
                        game.getJSONObject("left_team").getString("name"),
                        game.getJSONObject("left_team").getString("score"),
                        game.getJSONObject("right_team").getString("score"),
                        game.getJSONObject("right_team").getString("name"),
                        game.getString("match_status_cn")
                        ));
            }
        }
        message.setData(sb.toString());
        return message;
    }

    @Handler(info = "查看比赛详情")
    public TextMessage detail(@Param("比赛ID") String id) {
        TextMessage message = new TextMessage();
        message.setTitle("比赛详情");
        StringBuilder sb = new StringBuilder();
        String infoStr = HttpUtils.doGet(String.format("https://m.zhibo8.cc/json/match/%s.htm", id));
        if (infoStr == null) {
            message.setData("当前比赛不存在");
            return message;
        }
        JSONObject info = JSON.parseObject(infoStr);
        String infoMsg = String.format("[%s]\n%s vs %s\n",
                info.getJSONObject("league").getString("name_cn"),
                info.getJSONObject("left_team").getString("name"),
                info.getJSONObject("right_team").getString("name")
        );
        sb.append(infoMsg);
        String resStr = HttpUtils.doGet(String.format("https://odds.duoduocdn.com/football/detail?id=%s&type=ou&cid=5", id));
        JSONArray oddsChange = JSON.parseObject(resStr).getJSONObject("data").getJSONArray("oddsChange");
        if (oddsChange.isEmpty()) {
            sb.append("暂无赔率信息");
        } else {
            JSONArray odds = oddsChange.getJSONObject(0).getJSONArray("row");
            sb.append(String.format("胜 %s 平 %s 负 %s",
                    odds.getJSONObject(0).getString("v"),
                    odds.getJSONObject(1).getString("v"),
                    odds.getJSONObject(2).getString("v")));
        }
        message.setData(sb.toString());
        return message;
    }

    @Handler(command = "下注", info = "小赌怡情，大赌伤身")
    public String bet(@Param("比赛ID") String id, @Param("胜平负") String winner, @Param("金额") String amount) {
        return null;
    }
}

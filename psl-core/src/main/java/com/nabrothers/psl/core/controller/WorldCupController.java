package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.context.Session;
import com.nabrothers.psl.sdk.context.SessionContext;
import com.nabrothers.psl.sdk.message.ImageMessage;
import com.nabrothers.psl.sdk.message.TextMessage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.util.*;

@Component
public class WorldCupController {
    @Handler(command = "抽卡", info = "【世界杯】获取一张2022卡塔尔世界杯球员卡")
    public ImageMessage getCard() {
        String res = HttpUtils.doGet("https://stats.qiumibao.com/data/json_v2/list/world_cup_%5Byear%5D.htm");
        int groupIndex = new Random().nextInt(8);
        JSONArray group = JSONObject.parseObject(res).getJSONArray("data").getJSONObject(groupIndex).getJSONArray("list");
        int teamIndex = new Random().nextInt(4);
        String teamId = group.getJSONObject(teamIndex).getString("teamId");
        String res2 = HttpUtils.doGet("https://db.qiumibao.com/f/v2/zuqiu/team/squad?id=" + teamId);
        List<String> playerIds = new ArrayList<>();
        JSONArray squads = JSONObject.parseObject(res2).getJSONObject("data").getJSONArray("squad");
        for (int i = 0; i < 4; i++) {
            JSONArray squad = squads.getJSONObject(i).getJSONArray("list");
            for (int j = 0; j < squad.size(); j++) {
                playerIds.add(squad.getJSONObject(j).getJSONObject("profile").getString("id"));
            }
        }
        String playerId = playerIds.get(new Random().nextInt(playerIds.size()));
        String res3 = HttpUtils.doGet("https://db.qiumibao.com/f/v2/zuqiu/player/base?id=" + playerId);
        JSONObject player = JSONObject.parseObject(res3).getJSONObject("data").getJSONObject("info");
        ImageMessage image = new ImageController().getImage(player.getString("name_cn"));
        image.setInfo(player.getString("name_cn") + " / " + player.getString("team_name")+ " / " + player.getString("base_str"));
        return image;
    }

    @Handler(command = "阵容", info = "【世界杯】获取你的2022卡塔尔世界杯专属阵容")
    public TextMessage getSquad() {
        TextMessage textMessage = new TextMessage();
        Map<String, List<Integer>> squadMap = new HashMap<>();
        squadMap.put("442", Arrays.asList(1,4,4,2));
        squadMap.put("433", Arrays.asList(1,4,3,3));
        squadMap.put("4231", Arrays.asList(1,4,5,1));
        squadMap.put("4321", Arrays.asList(1,4,5,1));
        squadMap.put("352", Arrays.asList(1,3,5,2));
        squadMap.put("343", Arrays.asList(1,3,4,3));
        squadMap.put("532", Arrays.asList(1,5,3,2));
        squadMap.put("541", Arrays.asList(1,5,4,1));
        StringBuilder sb = new StringBuilder();
        Session session = SessionContext.get();
        textMessage.setTitle(session.getSender().getNickname() + "的世界杯专属阵容");
        String squadKey = (String)squadMap.keySet().stream().toArray()[new Random().nextInt(squadMap.size())];
        sb.append("阵型：" + squadKey + "\n");
        for (int i = 0; i < 4; i++) {
            String position = null;
            if (i == 0) {
                position = "前锋";
            } else if (i == 1) {
                position = "中场";
            } else if (i == 2) {
                position = "后卫";
            } else if (i == 3) {
                position = "门将";
            }
            for (int count = 0; count < squadMap.get(squadKey).get(3-i); count++) {
                String res = HttpUtils.doGet("https://stats.qiumibao.com/data/json_v2/list/world_cup_%5Byear%5D.htm");
                int groupIndex = new Random().nextInt(8);
                JSONArray group = JSONObject.parseObject(res).getJSONArray("data").getJSONObject(groupIndex).getJSONArray("list");
                int teamIndex = new Random().nextInt(4);
                String teamId = group.getJSONObject(teamIndex).getString("teamId");
                String res2 = HttpUtils.doGet("https://db.qiumibao.com/f/v2/zuqiu/team/squad?id=" + teamId);
                JSONArray squads = JSONObject.parseObject(res2).getJSONObject("data").getJSONArray("squad");
                JSONArray squad = squads.getJSONObject(i).getJSONArray("list");
                int j = new Random().nextInt(squad.size());
                String playerId = squad.getJSONObject(j).getJSONObject("profile").getString("id");
                String res3 = HttpUtils.doGet("https://db.qiumibao.com/f/v2/zuqiu/player/base?id=" + playerId);
                JSONObject player = JSONObject.parseObject(res3).getJSONObject("data").getJSONObject("info");
                sb.append("【" + position + "】" + player.getString("name_cn") + " / " + player.getString("base_str") + "\n");
            }
        }
        textMessage.setData(sb.toString());
        return textMessage;
    }
}

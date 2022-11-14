package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.message.ImageMessage;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
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
}

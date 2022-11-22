package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.dao.BetRecordDAO;
import com.nabrothers.psl.core.dao.UserDAO;
import com.nabrothers.psl.core.dto.BetRecordDTO;
import com.nabrothers.psl.core.dto.UserDTO;
import com.nabrothers.psl.core.service.TransactionService;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Param;
import com.nabrothers.psl.sdk.context.SessionContext;
import com.nabrothers.psl.sdk.message.CQCode;
import com.nabrothers.psl.sdk.message.TextMessage;
import com.nabrothers.psl.sdk.service.MessageService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.transaction.TransactionException;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Controller
@Handler(command = "菠菜")
public class GambleController {
    @Resource
    private MessageService messageService;

    @Resource
    private BetRecordDAO betRecordDAO;

    @Resource
    private UserDAO userDAO;

    @Resource
    private TransactionService transactionService;

    private ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

    @PostConstruct
    public void init() {
        executor.scheduleWithFixedDelay(this::refreshMatchStatus, 0, 60, TimeUnit.SECONDS);
    }

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

        sb = new StringBuilder();
        List<BetRecordDTO> records = betRecordDAO.queryByMatchId(Long.valueOf(id));
        for (BetRecordDTO record : records) {
            UserDTO userDTO = userDAO.queryByUserId(record.getUserId());
            String expect;
            if (record.getExpect() == 1) {
                expect = info.getJSONObject("left_team").getString("name") + " 胜";
            } else if (record.getExpect() == 0) {
                expect = "打平";
            } else {
                expect = info.getJSONObject("right_team").getString("name") + " 胜";
            }
            sb.append(String.format("%s 下注 %s %s\n",
                    userDTO.getName(),
                    record.getAmount(),
                    expect
            ));
        }

        message.setFooter(sb.toString());
        return message;
    }

    @Handler(command = "下注", info = "小赌怡情，大赌伤身")
    public String bet(@Param("比赛ID") String id, @Param("胜平负") String winner, @Param("金额") String amount) {
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("胜", 1);
        resultMap.put("平", 0);
        resultMap.put("负", -1);

        if (!resultMap.containsKey(winner)) {
            return "参数错误";
        }

        if (!StringUtils.isNumeric(amount) || !StringUtils.isNumeric(id)) {
            return "参数错误";
        }

        if (Long.valueOf(amount) <= 0) {
            return "参数错误";
        }

        String infoStr = HttpUtils.doGet(String.format("https://m.zhibo8.cc/json/match/%s.htm", id));
        if (infoStr == null) {
            return "当前比赛不存在";
        }

        String resStr = HttpUtils.doGet(String.format("https://odds.duoduocdn.com/football/detail?id=%s&type=ou&cid=5", id));
        JSONArray oddsChange = JSON.parseObject(resStr).getJSONObject("data").getJSONArray("oddsChange");
        if (oddsChange.isEmpty()) {
            return "暂无赔率信息";
        }

        try {
            transactionService.deduct(SessionContext.get().getSender().getId(), Long.valueOf(amount));
        } catch (TransactionException e) {
            return e.getMessage();
        }

        JSONArray odds = oddsChange.getJSONObject(0).getJSONArray("row");

        BetRecordDTO betRecordDTO = new BetRecordDTO();
        betRecordDTO.setUserId(SessionContext.get().getSender().getId());
        betRecordDTO.setMatchId(Long.valueOf(id));
        betRecordDTO.setWin(Double.valueOf(odds.getJSONObject(0).getString("v")));
        betRecordDTO.setDraw(Double.valueOf(odds.getJSONObject(1).getString("v")));
        betRecordDTO.setLose(Double.valueOf(odds.getJSONObject(2).getString("v")));
        betRecordDTO.setExpect(resultMap.get(winner));
        betRecordDTO.setAmount(Long.valueOf(amount));

        betRecordDAO.insert(betRecordDTO);

        return "下注成功，请等待开奖";
    }

    private void refreshMatchStatus() {
        String retStr = HttpUtils.doGet("https://guess.qiumibao.com/saishi/zbbList?type=football_zc");
        JSONArray dateList = JSON.parseObject(retStr).getJSONObject("data").getJSONArray("list");
        for (Object dateObj : dateList) {
            JSONObject date = (JSONObject) dateObj;
            JSONArray games = date.getJSONArray("list");
            for (Object gameObj : games) {
                JSONObject game = (JSONObject) gameObj;
                Integer left = game.getJSONObject("left_team").getInteger("score");
                Integer right = game.getJSONObject("right_team").getInteger("score");
                int res = 0;
                if (left > right) {
                    res = 1;
                } else if (left < right) {
                    res = -1;
                }
                if (game.getString("is_finish").equals("1")) {
                    List<BetRecordDTO> records = betRecordDAO.queryByMatchId(game.getLong("saishi_id"));
                    for (BetRecordDTO record : records) {
                        if (record.getResult() == null) {
                            betRecordDAO.updateResultById(res, record.getId());
                            StringBuilder sb = new StringBuilder();
                            sb.append(String.format("[%s] 已完赛，%s %s-%s %s，",
                                    game.getString("saishi_id"),
                                    game.getJSONObject("left_team").getString("name"),
                                    game.getJSONObject("left_team").getString("score"),
                                    game.getJSONObject("right_team").getString("score"),
                                    game.getJSONObject("right_team").getString("name")
                                    ));
                            if (res == record.getExpect()) {
                                long money = 0;
                                if (res == 1) {
                                    money = (long) (record.getWin() * record.getAmount());
                                } else if (res == 0) {
                                    money = (long) (record.getDraw() * record.getAmount());
                                } else {
                                    money = (long) (record.getLose() * record.getAmount());
                                }
                                sb.append("恭喜您获得 " + money);
                                try {
                                    transactionService.add(record.getUserId(), money);
                                } catch (TransactionException ignore) {}
                            } else {
                                sb.append("再接再厉");
                            }
                            messageService.sendGroupMessage(383250309L, String.format(CQCode.AT_PATTERN, record.getUserId()) + sb.toString());
                        }
                    }

                }
            }
        }
    }
}

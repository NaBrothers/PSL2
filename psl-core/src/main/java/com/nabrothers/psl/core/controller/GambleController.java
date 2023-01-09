package com.nabrothers.psl.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nabrothers.psl.core.dao.BetRecordDAO;
import com.nabrothers.psl.core.dao.UserDAO;
import com.nabrothers.psl.core.dto.BetRecordDTO;
import com.nabrothers.psl.core.dto.UserDTO;
import com.nabrothers.psl.core.exception.TransactionException;
import com.nabrothers.psl.core.service.TransactionService;
import com.nabrothers.psl.core.utils.Config;
import com.nabrothers.psl.core.utils.HttpUtils;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Param;
import com.nabrothers.psl.sdk.context.SessionContext;
import com.nabrothers.psl.sdk.message.CQCode;
import com.nabrothers.psl.sdk.message.ImageMessage;
import com.nabrothers.psl.sdk.message.TextMessage;
import com.nabrothers.psl.sdk.service.MessageService;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
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

    private static ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

    @PostConstruct
    public void init() {
        executor.scheduleWithFixedDelay(this::refreshMatchStatus, 0, 60, TimeUnit.SECONDS);
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void dailyReward() {
        long amount = 100000L;
        List<UserDTO> users = userDAO.queryAll();
        for (UserDTO user : users) {
            try {
                transactionService.add(user.getUserId(), amount);
            } catch (Exception ignore) {}
        }
        messageService.sendGroupMessage(Config.DEFAULT_GROUP_ID, "今日菠菜资金 $" + amount + " 已到账，请查收");
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

        JSONObject info = JSONObject.parseObject(infoStr);
        Long startTime = Long.valueOf(info.getString("start_time")) * 1000;
        Long currentTime = System.currentTimeMillis();
        if (currentTime > startTime) {
            return "比赛开始后无法下注";
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
        Set<Long> betRecordSet = new HashSet<>();
        for (Object dateObj : dateList) {
            JSONObject date = (JSONObject) dateObj;
            JSONArray games = date.getJSONArray("list");
            for (Object gameObj : games) {
                JSONObject game = (JSONObject) gameObj;
                if (game.getString("is_finish").equals("1") || game.getIntValue("match_status") > 4) {
                    Integer left = game.getJSONObject("left_team").getInteger("score");
                    Integer right = game.getJSONObject("right_team").getInteger("score");
                    int res = 0;
                    if (left > right) {
                        res = 1;
                    } else if (left < right) {
                        res = -1;
                    }
                    List<BetRecordDTO> records = betRecordDAO.queryByMatchId(game.getLong("saishi_id"));
                    for (BetRecordDTO record : records) {
                        if (betRecordSet.contains(record.getId())) {
                            continue;
                        }
                        if (record.getResult() == null) {
                            betRecordSet.add(record.getId());
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
                            messageService.sendGroupMessage(Config.DEFAULT_GROUP_ID, String.format(CQCode.AT_PATTERN, record.getUserId()) + sb.toString());
                        }
                    }

                }
            }
        }
    }

    @Handler(command = "排名", info = "查看土豪排名")
    public TextMessage scoreboard() {
        TextMessage message = new TextMessage();
        message.setTitle("土豪榜");
        List<UserDTO> users = userDAO.queryAll();
        List<BetRecordDTO> records = betRecordDAO.queryAll();

        Map<Long, Long> betAmount = new HashMap<>();
        for (BetRecordDTO record : records) {
            if (record.getResult() == null) {
                betAmount.putIfAbsent(record.getUserId(), 0L);
                betAmount.put(record.getUserId(), betAmount.get(record.getUserId()) + record.getAmount());
            }
        }

        users.stream().forEach(user -> {
            if (betAmount.containsKey(user.getUserId())) {
                user.setMoney(user.getMoney() + betAmount.get(user.getUserId()));
            }
        });

        users = users.stream().sorted(Comparator.comparing(UserDTO::getMoney, Comparator.reverseOrder())).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (UserDTO user : users) {
            i++;
            sb.append(String.format("%d - [%d] %s $%d\n", i, user.getId(), user.getName(), user.getMoney()));
        }
        message.setData(sb.toString());
        long total = 0L;

        for (BetRecordDTO record : records) {
            if (record.getResult() == null) {
                continue;
            }
            if (record.getExpect().equals(record.getResult())) {
                if (record.getExpect() == 1) {
                    total -= (long) (record.getWin() * record.getAmount()) - record.getAmount();
                } else if (record.getExpect() == 0) {
                    total -= (long) (record.getDraw() * record.getAmount()) - record.getAmount();
                } else if (record.getExpect() == -1) {
                    total -= (long) (record.getLose() * record.getAmount()) - record.getAmount();
                }
            } else {
                total += record.getAmount();
            }
        }
        message.setFooter("庄家总盈亏：" + (total >= 0 ? "$" + total : "-$" + Math.abs(total)));
        return message;
    }

    @Handler(command = "排名 胜率", info = "查看胜率排名")
    public TextMessage winboard() {
        TextMessage message = new TextMessage();
        message.setTitle("胜率榜");
        List<UserDTO> users = userDAO.queryAll();
        List<BetRecordDTO> records = betRecordDAO.queryAll();

        Map<Long, Long> betCount = new HashMap<>();
        Map<Long, Long> winCount = new HashMap<>();
        Map<Long, Set<String>> matchSet = new HashMap<>();

        for (UserDTO user : users) {
            betCount.putIfAbsent(user.getUserId(), 0L);
            winCount.putIfAbsent(user.getUserId(), 0L);
            matchSet.putIfAbsent(user.getUserId(), new HashSet<>());
        }

        for (BetRecordDTO record : records) {
            if (record.getResult() != null) {
                String key = record.getMatchId() + "_" + record.getExpect();
                if (matchSet.get(record.getUserId()).contains(key)) {
                    continue;
                }
                matchSet.get(record.getUserId()).add(key);
                betCount.put(record.getUserId(), betCount.get(record.getUserId()) + 1);
                if (record.getExpect().equals(record.getResult())) {
                    winCount.put(record.getUserId(), winCount.get(record.getUserId()) + 1);
                }
            }
        }

        users = users.stream().sorted((a,b) -> {
            if (betCount.get(a.getUserId()) == 0) {
                return 1;
            }
            if (betCount.get(b.getUserId()) == 0) {
                return 1;
            }
            double rateA = 1.0 * winCount.get(a.getUserId()) / betCount.get(a.getUserId());
            double rateB = 1.0 * winCount.get(b.getUserId()) / betCount.get(b.getUserId());
            if (rateA < rateB) {
                return 1;
            } else if (rateA > rateB) {
                return -1;
            } else {
                return betCount.get(b.getUserId()).compareTo(betCount.get(a.getUserId()));
            }
        }).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (UserDTO user : users) {
            i++;
            double rate = (1.0 * winCount.get(user.getUserId()) / betCount.get(user.getUserId())) * 100;
            sb.append(String.format("%d - [%d] %s %d手 %d%%\n", i, user.getId(), user.getName(), betCount.get(user.getUserId()), (long) rate));
        }
        message.setData(sb.toString());

        return message;
    }

    @Handler(command = "盈亏", info = "查看盈亏趋势")
    public ImageMessage showPlot() throws IOException {
        ImageMessage message = new ImageMessage();
        Map<Long, String> users = userDAO.queryAll().stream().collect(Collectors.toMap(UserDTO::getUserId, UserDTO::getName));

        List<BetRecordDTO> records = betRecordDAO.queryAll();

        records = records.stream()
                .filter(record -> record.getResult() != null)
                .sorted(Comparator.comparing(BetRecordDTO::getUpdateTime))
                .collect(Collectors.toList());

        Map<Long, Map<Long, Long>> changeList = new HashMap<>();

        List<Long> matchSeq = new ArrayList<>();
        matchSeq.add(0L);

        for (BetRecordDTO record : records) {
            if (!matchSeq.contains(record.getMatchId())) {
                matchSeq.add(record.getMatchId());
            }
            changeList.putIfAbsent(record.getUserId(), new HashMap<>());
            Map<Long, Long> change = changeList.get(record.getUserId());
            change.putIfAbsent(record.getMatchId(), 0L);
            long amount = 0L;
            if (record.getExpect().equals(record.getResult())) {
                if (record.getExpect() == 1) {
                    amount = (long) (record.getWin() * record.getAmount());
                } else if (record.getExpect() == 0) {
                    amount = (long) (record.getDraw() * record.getAmount());
                } else if (record.getExpect() == -1) {
                    amount = (long) (record.getLose() * record.getAmount());
                }
            }
            amount -= record.getAmount();
            change.put(record.getMatchId(), change.get(record.getMatchId()) + amount);
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Long userId : changeList.keySet()) {
            Map<Long, Long> change = changeList.get(userId);
            long sum = 0L;
            for (Long matchId : matchSeq) {
                if (change.containsKey(matchId)) {
                    sum += change.get(matchId);
                }
                dataset.addValue(sum, users.get(userId) + "          ", matchId);
            }
        }

        // 创建JFreeChart对象
        JFreeChart chart = ChartFactory.createLineChart(
                "盈亏变化", // 图标题
                "比赛", // x轴标题
                "盈亏", // y轴标题
                dataset, //数据集
                PlotOrientation.VERTICAL, //图表方向
                true, true, false);

        chart.getPlot().setBackgroundPaint(Color.WHITE);
        for (int i = 0; i < changeList.keySet().size(); i++) {
            ((CategoryPlot) chart.getPlot()).getRenderer().setSeriesStroke(i, new BasicStroke(3.0f));
        }

        ((CategoryPlot) chart.getPlot()).getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);

        ChartUtils.saveChartAsJPEG(new File("./go-cqhttp/data/images/cache/chart.jpg"), chart, 38 * matchSeq.size(), 1024);
        message.setUrl("cache/chart.jpg");
        return message;
    }
}

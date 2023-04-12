package com.nabrothers.psl.core.controller;

import com.google.common.base.Joiner;
import com.nabrothers.psl.core.dao.BilliardGameDAO;
import com.nabrothers.psl.core.dao.BilliardRecordDAO;
import com.nabrothers.psl.core.dao.UserDAO;
import com.nabrothers.psl.core.dto.BilliardGameDTO;
import com.nabrothers.psl.core.dto.BilliardRecordDTO;
import com.nabrothers.psl.core.dto.UserDTO;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Param;
import com.nabrothers.psl.sdk.message.ImageMessage;
import com.nabrothers.psl.sdk.message.TextMessage;
import com.nabrothers.psl.sdk.service.CacheService;
import com.nabrothers.psl.sdk.service.MessageService;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.awt.Color;
import java.awt.BasicStroke;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Handler(command = "台球")
public class BilliardController {
    @Resource
    private CacheService cacheService;

    @Resource
    private MessageService messageService;

    @Resource
    private UserDAO userDAO;

    @Resource
    private BilliardRecordDAO billiardRecordDAO;

    @Resource
    private BilliardGameDAO billiardGameDAO;

    private static Map<Integer, String> gameTypeMap = new HashMap<>();

    private static Map<String, Integer> typeGameMap = new HashMap<>();

    static {
        gameTypeMap.put(0, "小组赛");
        gameTypeMap.put(1, "胜者组");
        gameTypeMap.put(2, "败者组");
        gameTypeMap.put(3, "决赛");
        gameTypeMap.put(4, "友谊赛");

        gameTypeMap.entrySet().stream().forEach(entry -> typeGameMap.put(entry.getValue(), entry.getKey()));
    }

    @Handler(info = "GBL积分榜")
    public TextMessage billiard() {
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("Geeks Billiard League");
        Map<Long, Integer> playersMap = new HashMap<>();

        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll();
        for (BilliardRecordDTO it : brList) {
            Integer winnerPoints = 0;
            String[] winnerArray = it.getWinnerId().split(",");
            for (int i = 0; i < winnerArray.length; i++) {
                Long userid = Long.valueOf(winnerArray[i]);
                if (!playersMap.containsKey(userid)) {
                    playersMap.put(userid, Integer.valueOf(cacheService.get("billiard", "originalPoints")));
                }
                winnerPoints += playersMap.get(userid);
            }
            winnerPoints /= winnerArray.length;

            Integer loserPoints = 0;
            String[] loserArray = it.getLoserId().split(",");
            for (int i = 0; i < loserArray.length; i++) {
                Long userid = Long.valueOf(loserArray[i]);
                if (!playersMap.containsKey(userid)) {
                    playersMap.put(userid, Integer.valueOf(cacheService.get("billiard", "originalPoints")));
                }
                loserPoints += playersMap.get(userid);
            }
            loserPoints /= loserArray.length;

            double dr = winnerPoints - loserPoints;
            double we = 1 / (Math.pow(10, (-dr / Integer.valueOf(cacheService.get("billiard", "weCo")))) + 1);

            Integer gameCo = Integer.valueOf(cacheService.get("billiard", gameTypeMap.get(it.getGameType())));

            for (int i = 0; i < winnerArray.length; i++) {
                Long userid = Long.valueOf(winnerArray[i]);
                playersMap.put(userid,
                        (int) Math.round((playersMap.get(userid) + gameCo * (1 - we))));
            }

            for (int i = 0; i < loserArray.length; i++) {
                Long userid = Long.valueOf(loserArray[i]);
                playersMap.put(userid, (int) Math.round((playersMap.get(userid)
                        + Integer.valueOf(cacheService.get("billiard", "loserCo")) * (we - 1))));
            }
        }

        List<Map.Entry<Long, Integer>> entrys = new ArrayList<>(playersMap.entrySet());
        Collections.sort(entrys, (Map.Entry<Long, Integer> a, Map.Entry<Long, Integer> b) -> {
            return b.getValue().compareTo(a.getValue());
        });

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<Long, Integer> entry : entrys) {
            i++;
            Long userid = entry.getKey();
            UserDTO user = userDAO.queryByUserId(userid);
            sb.append(String.format("%d - [%d] %s %d\n", i, user.getId(), user.getName(), entry.getValue()));
        }

        textMessage.setData(sb.toString());

        return textMessage;
    }

    @Handler(command = "记录")
    public String billiardRecord(@Param("比赛类型") String type, @Param("胜者") String winner, @Param("胜者得分") String scoreW,
            @Param("败者") String loser, @Param("败者得分") String scoreL) {

        if (!typeGameMap.containsKey(type)) {
            return "参数错误";
        }

        if (Integer.valueOf(scoreW) < 0 || Integer.valueOf(scoreL) < 0) {
            return "参数错误";
        }

        String[] winnerArray = winner.split(",|，");
        String[] loserArray = loser.split(",|，");
        List<Long> winners = new ArrayList<>();
        List<Long> losers = new ArrayList<>();
        for (int i = 0; i < winnerArray.length; i++) {
            UserDTO winnerDTO = userDAO.queryByAlias(winnerArray[i]);
            if (winnerDTO == null) {
                return "参数错误";
            }
            winners.add(winnerDTO.getId());
        }

        for (int i = 0; i < loserArray.length; i++) {
            UserDTO loserDTO = userDAO.queryByAlias(loserArray[i]);
            if (loserDTO == null) {
                return "参数错误";
            }
            losers.add(loserDTO.getId());
        }

        BilliardRecordDTO billiardRecordDTO = new BilliardRecordDTO();
        billiardRecordDTO.setGameType(typeGameMap.get(type));
        billiardRecordDTO.setWinnerId(Joiner.on(',').join(winners));
        billiardRecordDTO.setLoserId(Joiner.on(',').join(losers));
        billiardRecordDTO.setScoreW(Integer.valueOf(scoreW));
        billiardRecordDTO.setScoreL(Integer.valueOf(scoreL));

        billiardRecordDAO.insert(billiardRecordDTO);

        return "记录成功";
    }

    @Handler(command = "选手")
    public TextMessage queryByUserName(@Param("选手") String username) {
        TextMessage textMessage = new TextMessage();
        StringBuilder sb = new StringBuilder();
        UserDTO userDTO = userDAO.queryByAlias(username);
        if (userDTO == null) {
            textMessage.setData("无比赛记录");
            return textMessage;
        }
        textMessage.setTitle(userDTO.getName() + " 比赛记录");
        Long userid = userDTO.getUserId();
        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll();
        Map<Long, BilliardGameDTO> bgMap = billiardGameDAO.queryAll().stream().collect(Collectors.toMap(BilliardGameDTO::getId, Function.identity()));
        Map<Long, String> playerMap = new HashMap<>();

        for (BilliardRecordDTO br : brList) {
            List<String> players = new ArrayList<>();
            players.addAll(Arrays.asList(br.getWinnerId().split(",")));
            players.addAll(Arrays.asList(br.getLoserId().split(",")));
            for (String p : players) {
                if (!playerMap.containsKey(Long.valueOf(p))) {
                    playerMap.put(Long.valueOf(p), userDAO.queryByUserId(Long.valueOf(p)).getName());
                }
            }
        }

        Map<Long, List<BilliardRecordDTO>> records = new TreeMap<>();

        for (BilliardRecordDTO br : brList) {
            List<String> players = new ArrayList<>();
            String[] winner = br.getWinnerId().split(",");
            String[] loser = br.getLoserId().split(",");
            players.addAll(Arrays.asList(winner));
            players.addAll(Arrays.asList(loser));
            if (players.contains(String.valueOf(userid))) {
                records.putIfAbsent(br.getGameId(), new ArrayList<>());
                records.get(br.getGameId()).add(br);
            }
        }

        Map<Long, Double> pointsDifferMap = getPointsDifferMap();
        Integer loserCo = Integer.valueOf(cacheService.get("billiard", "loserCo"));
        Map<Integer, Integer> gameCoMap = new HashMap<>();
        for(Integer gt : gameTypeMap.keySet()) {
            gameCoMap.put(gt, Integer.valueOf(cacheService.get("billiard", gameTypeMap.get(gt))));
        }

        for (Map.Entry<Long, List<BilliardRecordDTO>> record : records.entrySet()) {
            BilliardGameDTO bg = bgMap.get(record.getKey());
            sb.append(String.format("-- [%d] %s %s --\n", bg.getId(), bg.getName(), bg.getDate()));
            List<BilliardRecordDTO> brs = record.getValue();
            for (BilliardRecordDTO br : brs) {
                double we = pointsDifferMap.get(br.getId());
                Integer gameCo = gameCoMap.get(br.getGameType());                

                List<String> winners = Arrays.asList(br.getWinnerId().split(","));
                String scoreDiffer;
                String[] winner = br.getWinnerId().split(",");
                String[] loser = br.getLoserId().split(",");

                String[] wns = new String[winner.length];
                String[] lns = new String[loser.length];
                for (int j = 0; j < winner.length; j++) {
                    wns[j] = playerMap.get(Long.valueOf(winner[j]));
                }
                for (int j = 0; j < loser.length; j++) {
                    lns[j] = playerMap.get(Long.valueOf(loser[j]));
                }
                if (winners.contains(String.valueOf(userid))) {
                    scoreDiffer = "+" + (int) (gameCo * (1 - we));
                } else {
                    scoreDiffer = String.valueOf((int) (loserCo * (we - 1)));
                }
                sb.append("[" + gameTypeMap.get(br.getGameType()) + "] " + String.join(",", wns)
                        + " " + String.valueOf(br.getScoreW())
                        + " - " + String.valueOf(br.getScoreL()) + " "
                        + String.join(",", lns)
                        + " (" + scoreDiffer + ")" + "\n");
            }
            sb.append("\n");
        }

        textMessage.setData(sb.toString());

        return textMessage;
    }

    @Handler(command = "比赛")
    public TextMessage queryGame() {
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("比赛记录");
        StringBuilder sb = new StringBuilder();

        List<BilliardGameDTO> games = billiardGameDAO.queryAll();

        for (BilliardGameDTO game : games) {
            sb.append(String.format("-- [%d] %s %s --\n", game.getId(), game.getName(), game.getDate()));
            TextMessage msg = querySeriesGame(game.getId().toString());
            sb.append(msg.getData() + "\n");
        }

        textMessage.setData(sb.toString() + "\n");

        return textMessage;
    }

    @Handler(command = "比赛")
    public TextMessage querySeriesGame(@Param("比赛ID") String id) {
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("系列赛");
        StringBuilder sb = new StringBuilder();

        BilliardGameDTO game = billiardGameDAO.queryById(Long.valueOf(id));

        sb.append(String.format("[%d] %s\n", game.getId(), game.getName()));
        sb.append("- 时间：" + game.getDate() + "\n");
        sb.append("- 地点：" + game.getLocation() + "\n");
        sb.append("- 选手：");

        Map<Long, String> playerMap = new HashMap<>();

        for (String userId : game.getPlayers().split(",")) {
            playerMap.put(Long.valueOf(userId), userDAO.queryByUserId(Long.valueOf(userId)).getName());
        }

        sb.append(Joiner.on(",").join(playerMap.values()) + "\n");
        if (StringUtils.isNotEmpty(game.getRemark())) {
            sb.append("- 说明：" + game.getRemark() + "\n");
        }

        textMessage.setHeader(sb.toString());

        sb = new StringBuilder();

        List<BilliardRecordDTO> brList = billiardRecordDAO.queryByGameId(Long.valueOf(id));

        for (BilliardRecordDTO br : brList) {
            List<String> players = new ArrayList<>();
            String[] winner = br.getWinnerId().split(",");
            String[] loser = br.getLoserId().split(",");
            players.addAll(Arrays.asList(winner));
            players.addAll(Arrays.asList(loser));

            String[] wns = new String[winner.length];
            String[] lns = new String[loser.length];
            for (int j = 0; j < winner.length; j++) {
                wns[j] = playerMap.get(Long.valueOf(players.get(j)));
            }
            for (int j = 0; j < loser.length; j++) {
                lns[j] = playerMap.get(Long.valueOf(players.get(j + winner.length)));
            }
            sb.append("[" + gameTypeMap.get(br.getGameType()) + "] " + String.join(",", wns)
                    + " " + String.valueOf(br.getScoreW()) + " - "
                    + String.valueOf(br.getScoreL())
                    + " " + String.join(",", lns) + "\n");
        }

        textMessage.setData(sb.toString());
        return textMessage;
    }

    private Map<Long, Double> getPointsDifferMap() {
        Map<Long, Double> pointsDiffMap = new HashMap<>();
        Map<Long, Integer> playersMap = new HashMap<>();
        Map<Integer, Integer> gameCoMap = new HashMap<>();
        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll();

        for(Integer gt : gameTypeMap.keySet()) {
            gameCoMap.put(gt, Integer.valueOf(cacheService.get("billiard", gameTypeMap.get(gt))));
        }

        Integer op = Integer.valueOf(cacheService.get("billiard", "originalPoints"));
        Integer weCo = Integer.valueOf(cacheService.get("billiard", "weCo"));
        Integer loseCo = Integer.valueOf(cacheService.get("billiard", "loserCo"));

        for (BilliardRecordDTO it : brList) {
            Integer winnerPoints = 0;
            String[] winnerArray = it.getWinnerId().split(",");
            for (int i = 0; i < winnerArray.length; i++) {
                Long userid = Long.valueOf(winnerArray[i]);
                if (!playersMap.containsKey(userid)) {
                    playersMap.put(userid, op);
                }
                winnerPoints += playersMap.get(userid);
            }
            winnerPoints /= winnerArray.length;

            Integer loserPoints = 0;
            String[] loserArray = it.getLoserId().split(",");
            for (int i = 0; i < loserArray.length; i++) {
                Long userid = Long.valueOf(loserArray[i]);
                if (!playersMap.containsKey(userid)) {
                    playersMap.put(userid, op);
                }
                loserPoints += playersMap.get(userid);
            }
            loserPoints /= loserArray.length;

            double dr = winnerPoints - loserPoints;
            double we = 1 / (Math.pow(10, (-dr / weCo)) + 1);

            Integer gameCo = gameCoMap.get(it.getGameType());

            for (int i = 0; i < winnerArray.length; i++) {
                Long userid = Long.valueOf(winnerArray[i]);
                playersMap.put(userid,
                        (int) Math.round((playersMap.get(userid) + gameCo * (1 - we))));
            }

            for (int i = 0; i < loserArray.length; i++) {
                Long userid = Long.valueOf(loserArray[i]);
                playersMap.put(userid, (int) Math.round((playersMap.get(userid)
                        + loseCo * (we - 1))));
            }

            pointsDiffMap.put(it.getId(), we);
        }

        return pointsDiffMap;
    }

    @Handler(command = "积分曲线")
    public ImageMessage showPlot() throws IOException {
        ImageMessage message = new ImageMessage();
        Map<Long, Double> pointsDiffMap = getPointsDifferMap();
        Map<Long, String> playerMap = new HashMap<>();
        Map<Integer, Integer> gameCoMap = new HashMap<>();

        for(Integer gt : gameTypeMap.keySet()) {
            gameCoMap.put(gt, Integer.valueOf(cacheService.get("billiard", gameTypeMap.get(gt))));
        }
        Integer loseCo = Integer.valueOf(cacheService.get("billiard", "loserCo"));

        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll();
        Map<Long, Map<Long, Integer>> changeList = new HashMap<>();
        List<Long> gameSeq = new ArrayList<>();
        gameSeq.add(0L);

        for(BilliardRecordDTO br : brList) {
            if(!gameSeq.contains(br.getId())) {
                gameSeq.add(br.getId());
            }
            List<String> winners = Arrays.asList(br.getWinnerId().split(","));
            List<String> losers = Arrays.asList(br.getLoserId().split(","));
            for(String winner : winners) {
                long wid = Long.valueOf(winner);
                playerMap.putIfAbsent(wid, userDAO.queryByUserId(wid).getName());
                changeList.putIfAbsent(wid, new HashMap<>());
                Map<Long, Integer> change = changeList.get(wid);
                Integer point = (int)((1 - pointsDiffMap.get(br.getId())) * gameCoMap.get(br.getGameType()));
                change.put(br.getId(), point);
            }
            for(String loser : losers) {
                long lid = Long.valueOf(loser);
                playerMap.putIfAbsent(lid, userDAO.queryByUserId(lid).getName());
                changeList.putIfAbsent(lid, new HashMap<>());
                Map<Long, Integer> change = changeList.get(lid);
                Integer point = (int)((pointsDiffMap.get(br.getId()) - 1) * loseCo);
                change.put(br.getId(), point);
            }
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for(Long id : changeList.keySet()) {
            Map<Long, Integer> change = changeList.get(id);
            Integer sum = 0;
            for(Long gid : gameSeq) {
                if(change.containsKey(gid)) {
                    sum += change.get(gid);
                }
                dataset.addValue(sum, playerMap.get(id) + "        ", gid);
            }            
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "积分曲线", 
                "比赛", 
                "积分", 
                dataset, 
                PlotOrientation.VERTICAL,
                true, true, false);

        chart.getPlot().setBackgroundPaint(Color.WHITE);
        for(int i = 0; i < changeList.keySet().size(); i++) {
            ((CategoryPlot) chart.getPlot()).getRenderer().setSeriesStroke(i, new BasicStroke(3.0f));
        }

        ((CategoryPlot) chart.getPlot()).getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);

        ChartUtils.saveChartAsJPEG(new File("./go-cqhttp/data/images/cache/chart.jpg"), chart, 38 * gameSeq.size(), 1024);
        message.setUrl("cache/chart.jpg");

        return message;
    }
}

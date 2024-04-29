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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
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

    private String calcScoreBoard(Map<Long, Integer> playersMap, List<BilliardRecordDTO> brList) {
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
                        (int) Math.round(playersMap.get(userid) + (gameCo * (1 - we)) / winnerArray.length));
            }

            for (int i = 0; i < loserArray.length; i++) {
                Long userid = Long.valueOf(loserArray[i]);
                playersMap.put(userid, (int) Math.round(playersMap.get(userid)
                        + Integer.valueOf(cacheService.get("billiard", "loserCo")) * (we - 1) / loserArray.length));
            }
        }

        List<Map.Entry<Long, Integer>> entrys = new ArrayList<>(playersMap.entrySet());
        Collections.sort(entrys, (a, b) -> b.getValue().compareTo(a.getValue()));

        return printScoreBoard(entrys);
    }

    private String printScoreBoard(List<Map.Entry<Long, Integer>> entrys) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<Long, Integer> entry : entrys) {
            i++;
            Long userid = entry.getKey();
            UserDTO user = userDAO.queryByUserId(userid);
            sb.append(String.format("%d - [%d] %s %d\n", i, user.getId(), user.getName(), entry.getValue()));
        }

        return sb.toString();
    }

    @Handler(info = "GBL总积分榜")
    public TextMessage billiard() {
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("Geeks Billiard League 封神榜");
        textMessage.setData(calcScoreBoard(new HashMap<>(), billiardRecordDAO.queryAll(season)));
        return textMessage;
    }

    @Handler(command = "风云榜", info = "GBL近十场积分榜")
    public TextMessage billiardScoreBoardLast10() {
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("Geeks Billiard League 风云榜");
        textMessage.setData(calcScoreBoard(new HashMap<>(), billiardRecordDAO.queryLastN(10, season)));
        return textMessage;
    }

    @Handler(command = "慕斯伟罗榜", info = "GBL友谊赛积分榜")
    public TextMessage billiardScoreBoardFriendly() {
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("Geeks Billiard League 慕斯伟罗榜");
        textMessage.setData(calcScoreBoard(new HashMap<>(), billiardRecordDAO.queryFriendly(season)));
        return textMessage;
    }

    @Handler(command = "卡哇伊榜", info = "GBL正赛积分榜")
    public TextMessage billiardScoreBoardChampionship() {
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("Geeks Billiard League 卡哇伊榜");
        textMessage.setData(calcScoreBoard(new HashMap<>(), billiardRecordDAO.queryChampionship(season)));
        return textMessage;
    }

    @Handler(command = "cjb", info = "GBL友谊赛和正赛分差榜")
    public TextMessage billiardScoreBoardCjb() {
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("Geeks Billiard League CJB榜");
        Map<Long, Integer> friendly = new HashMap<>();
        Map<Long, Integer> championship = new HashMap<>();

        Map<Integer, String> cjbGameTypeMap = new HashMap<>();
        gameTypeMap.forEach((key, value) -> cjbGameTypeMap.put(key, "友谊赛"));
        Map<Integer, String> tmp = gameTypeMap;
       	gameTypeMap = cjbGameTypeMap;	
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        calcScoreBoard(friendly, billiardRecordDAO.queryFriendly(season));
        calcScoreBoard(championship, billiardRecordDAO.queryChampionship(season));

        Map<Long, Integer> playersMap = new HashMap<>();
        friendly.forEach((key, value) -> playersMap.put(key, value));
        championship.forEach((key, value) -> {
            if (!playersMap.containsKey(key)) {
                playersMap.put(key, Integer.valueOf(cacheService.get("billiard", "originalPoints")));
            }
            playersMap.put(key, playersMap.get(key) - value);
        });

        List<Map.Entry<Long, Integer>> entrys = new ArrayList<>(playersMap.entrySet());
        Collections.sort(entrys, (a, b) -> b.getValue().compareTo(a.getValue()));

        textMessage.setData(printScoreBoard(entrys));
	    gameTypeMap = tmp;
        return textMessage;
    }

    @Handler(command = "兹拉坦榜", info = "GBL小组赛积分榜")
    public TextMessage billiardScoreBoardGroupStage() {
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("Geeks Billiard League 兹拉坦榜");
        textMessage.setData(calcScoreBoard(new HashMap<>(), billiardRecordDAO.queryGameTypeScope(0, 0, season)));
        return textMessage;
    }

    @Handler(command = "巴特勒榜", info = "GBL淘汰赛积分榜")
    public TextMessage billiardScoreBoardEliminateStage() {
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("Geeks Billiard League 巴特勒榜");
        textMessage.setData(calcScoreBoard(new HashMap<>(), billiardRecordDAO.queryGameTypeScope(1, 3, season)));
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
            winners.add(winnerDTO.getUserId());
        }

        for (int i = 0; i < loserArray.length; i++) {
            UserDTO loserDTO = userDAO.queryByAlias(loserArray[i]);
            if (loserDTO == null) {
                return "参数错误";
            }
            losers.add(loserDTO.getUserId());
        }
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        BilliardRecordDTO billiardRecordDTO = new BilliardRecordDTO();
        billiardRecordDTO.setGameType(typeGameMap.get(type));
        billiardRecordDTO.setWinnerId(Joiner.on(',').join(winners));
        billiardRecordDTO.setLoserId(Joiner.on(',').join(losers));
        billiardRecordDTO.setScoreW(Integer.valueOf(scoreW));
        billiardRecordDTO.setScoreL(Integer.valueOf(scoreL));
        billiardRecordDTO.setGameId(Long.valueOf(cacheService.get("billiard", "currentGame")));
        billiardRecordDTO.setSeason(season);
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
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll(season);
        Map<Long, BilliardGameDTO> bgMap = billiardGameDAO.queryAll(
                season).stream()
                .collect(Collectors.toMap(BilliardGameDTO::getId, Function.identity()));
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
        for (Integer gt : gameTypeMap.keySet()) {
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
                    scoreDiffer = "+" + (int) (gameCo * (1 - we) / winner.length);
                } else {
                    scoreDiffer = String.valueOf((int) (loserCo * (we - 1) / loser.length));
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
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        List<BilliardGameDTO> games = billiardGameDAO.queryAll(season);

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

    @Handler(command = "比赛 添加")
    public String addSeriesGame(@Param("比赛名") String name, @Param("地点") String location, @Param("选手") String players) {
        List<Long> users = new ArrayList<>();
        for (String alias : players.split(",")) {
            UserDTO user = userDAO.queryByAlias(alias);
            if (user == null) {
                continue;
            }
            users.add(user.getUserId());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        BilliardGameDTO game = new BilliardGameDTO();
        game.setName(name);
        game.setLocation(location);
        game.setPlayers(Joiner.on(",").join(users));
        game.setDate(sdf.format(new Date()));
        game.setSeason(season);
        Long id = billiardGameDAO.insert(game);
        if (id > 0) {
            List<BilliardGameDTO> games = billiardGameDAO.queryAll(season);
            Collections.sort(games, Comparator.comparing(BilliardGameDTO::getId));
            cacheService.put("billiard", "currentGame", games.get(games.size() - 1).getId().toString());
        }
        return "添加成功";
    }

    private Map<Long, Double> getPointsDifferMap() {
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        Map<Long, Double> pointsDiffMap = new HashMap<>();
        Map<Long, Integer> playersMap = new HashMap<>();
        Map<Integer, Integer> gameCoMap = new HashMap<>();
        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll(season);

        for (Integer gt : gameTypeMap.keySet()) {
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
                        (int) Math.round((playersMap.get(userid) + gameCo * (1 - we) / winnerArray.length)));
            }

            for (int i = 0; i < loserArray.length; i++) {
                Long userid = Long.valueOf(loserArray[i]);
                playersMap.put(userid, (int) Math.round((playersMap.get(userid)
                        + loseCo * (we - 1) / loserArray.length)));
            }

            pointsDiffMap.put(it.getId(), we);
        }

        return pointsDiffMap;
    }

    @Handler(command = "积分 同期")
    public ImageMessage showHomochronousPlot() throws IOException {
        ImageMessage message = new ImageMessage();
        Map<Long, Double> pointsDiffMap = getPointsDifferMap();
        Map<Long, String> playerMap = new HashMap<>();
        Map<Integer, Integer> gameCoMap = new HashMap<>();
        for (Integer gt : gameTypeMap.keySet()) {
            gameCoMap.put(gt, Integer.valueOf(cacheService.get("billiard", gameTypeMap.get(gt))));
        }
        Integer loseCo = Integer.valueOf(cacheService.get("billiard", "loserCo"));
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll(season);
        Map<Long, Map<Long, Integer>> changeList = new LinkedHashMap<>();
        List<Long> gameSeq = new ArrayList<>();
        gameSeq.add(0L);
        // 缩减展示横轴数量
        int recordNumBeingDisplayed = 0;

        for (BilliardRecordDTO br : brList) {
            if (!gameSeq.contains(br.getId())) {
                gameSeq.add(br.getId());
            }
            List<String> winners = Arrays.asList(br.getWinnerId().split(","));
            List<String> losers = Arrays.asList(br.getLoserId().split(","));
            for (String winner : winners) {
                long wid = Long.valueOf(winner);
                playerMap.putIfAbsent(wid, userDAO.queryByUserId(wid).getName());
                changeList.putIfAbsent(wid, new HashMap<>());
                Map<Long, Integer> change = changeList.get(wid);
                Integer point = (int) ((1 - pointsDiffMap.get(br.getId())) * gameCoMap.get(br.getGameType()) / winners.size());
                change.put(Long.valueOf(change.size()), point);
                if (change.size() > recordNumBeingDisplayed) recordNumBeingDisplayed = change.size();
            }
            for (String loser : losers) {
                long lid = Long.valueOf(loser);
                playerMap.putIfAbsent(lid, userDAO.queryByUserId(lid).getName());
                changeList.putIfAbsent(lid, new HashMap<>());
                Map<Long, Integer> change = changeList.get(lid);
                Integer point = (int) ((pointsDiffMap.get(br.getId()) - 1) * loseCo / losers.size());
                change.put(Long.valueOf(change.size()), point);
                if (change.size() > recordNumBeingDisplayed) recordNumBeingDisplayed = change.size();
            }
        }
        // xydataset
        DefaultXYDataset xyDataset = new DefaultXYDataset();

        for (Long id : changeList.keySet()) {
            List<Double> xList = new ArrayList<>();
            List<Double> yList = new ArrayList<>();
            Map<Long, Integer> change = changeList.get(id);
            Integer sum = 0;
            for (Long i = 0L; i < change.size(); i++) {
                sum += change.get(i);
                xList.add(Double.valueOf(i));
                yList.add(Double.valueOf(sum.toString()));
            }
            // xydataset
            xyDataset.addSeries(playerMap.get(id) + "        ", new double[][]{ArrayUtils.toPrimitive(xList.toArray(new Double[0])), ArrayUtils.toPrimitive(yList.toArray(new Double[0]))});
        }

        JFreeChart xyChart = ChartFactory.createXYLineChart(
                "积分曲线",
                "比赛",
                "积分",
                xyDataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        xyChart.getPlot().setBackgroundPaint(Color.WHITE);
        NumberAxis numberAxis = (NumberAxis)((XYPlot)xyChart.getPlot()).getDomainAxis();
        numberAxis.setRange(brList.get(0).getId(), brList.get(brList.size()-1).getId());
        for (int i = 0; i < changeList.keySet().size(); i++) {
            ((XYPlot) xyChart.getPlot()).getRenderer().setSeriesStroke(i, new BasicStroke(5.0f));
        }
        ChartUtils.saveChartAsJPEG(new File("./go-cqhttp/data/images/cache/chart.jpg"), xyChart, 6 * recordNumBeingDisplayed,
                1024);

        message.setUrl("cache/chart.jpg");

        return message;
    }

    @Handler(command = "积分 同期系列赛")
    public ImageMessage showHomochronousSerialPlot() throws IOException {
        ImageMessage message = new ImageMessage();
        Map<Long, Double> pointsDiffMap = getPointsDifferMap();
        Map<Long, String> playerMap = new HashMap<>();
        Map<Integer, Integer> gameCoMap = new HashMap<>();
        Long currentGame = Long.valueOf(cacheService.get("billiard", "currentGame"));

        for (Integer gt : gameTypeMap.keySet()) {
            gameCoMap.put(gt, Integer.valueOf(cacheService.get("billiard", gameTypeMap.get(gt))));
        }
        Integer loseCo = Integer.valueOf(cacheService.get("billiard", "loserCo"));
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll(season);
        Map<Long, Map<Long, Integer>> changeList = new LinkedHashMap<>();
        List<Long> gameSeq = new ArrayList<>();
        gameSeq.add(0L);
        // 缩减展示横轴数量
        List<Long> recordIdBeingDisplayed = new ArrayList<>();
        Long tmpGameId = 0L;
        Long tmpRecordId = 0L;

        for (BilliardRecordDTO br : brList) {
            if (!gameSeq.contains(br.getId())) {
                gameSeq.add(br.getId());
            }
            if (br.getGameId() > tmpGameId && tmpRecordId != 0) {
                recordIdBeingDisplayed.add(tmpRecordId);
            }
            if (br.getGameId() == currentGame && br.equals(brList.get(brList.size()-1))) {
                recordIdBeingDisplayed.add(br.getId());
            }
            tmpGameId = br.getGameId();
            tmpRecordId = br.getId();
            List<String> winners = Arrays.asList(br.getWinnerId().split(","));
            List<String> losers = Arrays.asList(br.getLoserId().split(","));
            for (String winner : winners) {
                long wid = Long.valueOf(winner);
                playerMap.putIfAbsent(wid, userDAO.queryByUserId(wid).getName());
                changeList.putIfAbsent(wid, new HashMap<>());
                Map<Long, Integer> change = changeList.get(wid);
                Integer point = (int) ((1 - pointsDiffMap.get(br.getId())) * gameCoMap.get(br.getGameType()) / winners.size());
                change.put(br.getId(), point);
            }
            for (String loser : losers) {
                long lid = Long.valueOf(loser);
                playerMap.putIfAbsent(lid, userDAO.queryByUserId(lid).getName());
                changeList.putIfAbsent(lid, new HashMap<>());
                Map<Long, Integer> change = changeList.get(lid);
                Integer point = (int) ((pointsDiffMap.get(br.getId()) - 1) * loseCo / losers.size());
                change.put(br.getId(), point);
            }
        }

        // xydataset
        DefaultXYDataset xyDataset = new DefaultXYDataset();

        for (Long id : changeList.keySet()) {
            List<Double> xList = new ArrayList<>();
            List<Double> yList = new ArrayList<>();
            xList.add(0d);
            yList.add(0d);
            Map<Long, Integer> change = changeList.get(id);
            Integer sum = 0;
            for (Long gid : gameSeq) {
                boolean participated = false;
                if (change.containsKey(gid)) {
                    sum += change.get(gid);
                    participated = true;
                }
                if (recordIdBeingDisplayed.contains(gid) && participated) {
                    xList.add(Double.valueOf(xList.size()));
                    yList.add(Double.valueOf(sum.toString()));
                }
            }
            // xydataset
            xyDataset.addSeries(playerMap.get(id) + "        ", new double[][]{ArrayUtils.toPrimitive(xList.toArray(new Double[0])), ArrayUtils.toPrimitive(yList.toArray(new Double[0]))});
        }


        // xydataset
        JFreeChart xyChart = ChartFactory.createXYLineChart(
                "积分曲线",
                "比赛",
                "积分",
                xyDataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        xyChart.getPlot().setBackgroundPaint(Color.WHITE);
        NumberAxis numberAxis = (NumberAxis)((XYPlot)xyChart.getPlot()).getDomainAxis();
        numberAxis.setRange(brList.get(0).getId(), brList.get(brList.size()-1).getId());
        for (int i = 0; i < changeList.keySet().size(); i++) {
            ((XYPlot) xyChart.getPlot()).getRenderer().setSeriesStroke(i, new BasicStroke(5.0f));
        }
        ChartUtils.saveChartAsJPEG(new File("./go-cqhttp/data/images/cache/chart.jpg"), xyChart, 38 * recordIdBeingDisplayed.size(),
                1024);

        message.setUrl("cache/chart.jpg");

        return message;
    }

    @Handler(command = "积分")
    public ImageMessage showPlot() throws IOException {
        ImageMessage message = new ImageMessage();
        Map<Long, Double> pointsDiffMap = getPointsDifferMap();
        Map<Long, String> playerMap = new HashMap<>();
        Map<Integer, Integer> gameCoMap = new HashMap<>();
        Long currentGame = Long.valueOf(cacheService.get("billiard", "currentGame"));

        for (Integer gt : gameTypeMap.keySet()) {
            gameCoMap.put(gt, Integer.valueOf(cacheService.get("billiard", gameTypeMap.get(gt))));
        }
        Integer loseCo = Integer.valueOf(cacheService.get("billiard", "loserCo"));
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll(season);
        Map<Long, Map<Long, Integer>> changeList = new LinkedHashMap<>();
        List<Long> gameSeq = new ArrayList<>();
        gameSeq.add(0L);
        // 缩减展示横轴数量
        List<Long> recordIdBeingDisplayed = new ArrayList<>();
        Long tmpGameId = 0L;
        Long tmpRecordId = 0L;

        for (BilliardRecordDTO br : brList) {
            if (!gameSeq.contains(br.getId())) {
                gameSeq.add(br.getId());
            }
            if (br.getGameId() > tmpGameId && tmpRecordId != 0) {
                recordIdBeingDisplayed.add(tmpRecordId);
            }
            if (br.getGameId() == currentGame) {
                recordIdBeingDisplayed.add(br.getId());
            }
            tmpGameId = br.getGameId();
            tmpRecordId = br.getId();
            List<String> winners = Arrays.asList(br.getWinnerId().split(","));
            List<String> losers = Arrays.asList(br.getLoserId().split(","));
            for (String winner : winners) {
                long wid = Long.valueOf(winner);
                playerMap.putIfAbsent(wid, userDAO.queryByUserId(wid).getName());
                changeList.putIfAbsent(wid, new HashMap<>());
                Map<Long, Integer> change = changeList.get(wid);
                Integer point = (int) ((1 - pointsDiffMap.get(br.getId())) * gameCoMap.get(br.getGameType()) / winners.size());
                change.put(br.getId(), point);
            }
            for (String loser : losers) {
                long lid = Long.valueOf(loser);
                playerMap.putIfAbsent(lid, userDAO.queryByUserId(lid).getName());
                changeList.putIfAbsent(lid, new HashMap<>());
                Map<Long, Integer> change = changeList.get(lid);
                Integer point = (int) ((pointsDiffMap.get(br.getId()) - 1) * loseCo / losers.size());
                change.put(br.getId(), point);
            }
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // xydataset
        DefaultXYDataset xyDataset = new DefaultXYDataset();

        for (Long id : changeList.keySet()) {
            List<Double> xList = new ArrayList<>();
            List<Double> yList = new ArrayList<>();
            Map<Long, Integer> change = changeList.get(id);
            Integer sum = 0;
            for (Long gid : gameSeq) {
                if (change.containsKey(gid)) {
                    sum += change.get(gid);
                }
                if (recordIdBeingDisplayed.contains(gid) && Collections.min(change.keySet()) <= gid) {
                    dataset.addValue(sum, playerMap.get(id) + "        ", gid);
                }
                xList.add(Double.valueOf(gid.toString()));
                yList.add(Double.valueOf(sum.toString()));
            }
            // xydataset
            xyDataset.addSeries(playerMap.get(id) + "        ", new double[][]{ArrayUtils.toPrimitive(xList.toArray(new Double[0])), ArrayUtils.toPrimitive(yList.toArray(new Double[0]))});
        }

        

        // JFreeChart chart = ChartFactory.createLineChart(
        //         "积分曲线",
        //         "比赛",
        //         "积分",
        //         dataset,
        //         PlotOrientation.VERTICAL,
        //         true, true, false);

        // chart.getPlot().setBackgroundPaint(Color.WHITE);
        // for (int i = 0; i < changeList.keySet().size(); i++) {
        //     ((CategoryPlot) chart.getPlot()).getRenderer().setSeriesStroke(i, new BasicStroke(5.0f));
        // }

        // ((CategoryPlot) chart.getPlot()).getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);

        // ChartUtils.saveChartAsJPEG(new File("./go-cqhttp/data/images/cache/chart.jpg"), chart, 38 * recordIdBeingDisplayed.size(),
        //         1024);

        // xydataset
        JFreeChart xyChart = ChartFactory.createXYLineChart(
                "积分曲线",
                "比赛",
                "积分",
                xyDataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        xyChart.getPlot().setBackgroundPaint(Color.WHITE);
        NumberAxis numberAxis = (NumberAxis)((XYPlot)xyChart.getPlot()).getDomainAxis();
        numberAxis.setRange(brList.get(0).getId(), brList.get(brList.size()-1).getId());
        for (int i = 0; i < changeList.keySet().size(); i++) {
            ((XYPlot) xyChart.getPlot()).getRenderer().setSeriesStroke(i, new BasicStroke(5.0f));
        }
        ChartUtils.saveChartAsJPEG(new File("./go-cqhttp/data/images/cache/chart.jpg"), xyChart, 38 * recordIdBeingDisplayed.size(),
                1024);

        message.setUrl("cache/chart.jpg");

        return message;
    }

    @Handler(command = "删除记录")
    public String delete(Long i) {

        billiardRecordDAO.deleteById(i);

        return "删除成功";
    }

    @Handler(command = "胜率")
    public TextMessage winRate() {
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("胜率");
        StringBuilder sb = new StringBuilder();
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        Map<Long, Map<Integer, Integer[]>> playerMap = new HashMap<>();
        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll(season);
        for (BilliardRecordDTO br : brList) {
            List<String> winners = Arrays.asList(br.getWinnerId().split(","));
            List<String> losers = Arrays.asList(br.getLoserId().split(","));

            for (String wid : winners) {
                Long winner = Long.valueOf(wid);
                playerMap.putIfAbsent(winner, new HashMap<Integer, Integer[]>());
                Map<Integer, Integer[]> valMap = playerMap.get(winner);
                valMap.putIfAbsent(winners.size(), new Integer[]{0, 0, 0});
                Integer[] val = valMap.get(winners.size());
                val[0]++;
                val[2] += br.getScoreW();
            }
            for (String lid : losers) {
                Long loser = Long.valueOf(lid);
                playerMap.putIfAbsent(loser, new HashMap<Integer, Integer[]>());
                Map<Integer, Integer[]> valMap = playerMap.get(loser);
                valMap.putIfAbsent(losers.size(), new Integer[]{0, 0, 0});
                Integer[] val = valMap.get(losers.size());
                val[1]++;
                val[2] += br.getScoreL();
            }
        }

        for (Long playerId : playerMap.keySet()) {
            String name = userDAO.queryByUserId(playerId).getName();
            sb.append(name + ": \n");
            for (Integer type : playerMap.get(playerId).keySet()) {
                Integer[] val = playerMap.get(playerId).get(type);
                double score = (double) val[2] / ((double) val[0] + (double) val[1]);
                double rate = (double) val[0] / ((double) val[0] + (double) val[1]) * 100;
                sb.append(
                        String.format("%d v %d: %d 胜 %d 负 %.2f%% 场均得分: %.2f \n", type, type, val[0], val[1], rate, score));
            }
        }

        textMessage.setData(sb.toString());

        return textMessage;
    }

    @Handler(command = "胜率")
    public TextMessage personalWinRate(@Param("选手") String username) {
        TextMessage textMessage = new TextMessage();
        StringBuilder sb = new StringBuilder();
        UserDTO userDTO = userDAO.queryByAlias(username);
        if (userDTO == null) {
            textMessage.setData("无比赛记录");
            return textMessage;
        }
        textMessage.setTitle(userDTO.getName() + " 胜率");

        Map<Long, Integer[]> personalMatchResMap = new HashMap<>();
        Map<Long, Integer[]> personalDoublesMatchResMap = new HashMap<>();
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll(season);
        for (BilliardRecordDTO br : brList) {
            List<String> winners = Arrays.asList(br.getWinnerId().split(","));
            List<String> losers = Arrays.asList(br.getLoserId().split(","));
            if (winners.size() == 1 && losers.size() == 1) {
                if (!Long.valueOf(winners.get(0)).equals(userDTO.getUserId()) &&
                        !Long.valueOf(losers.get(0)).equals(userDTO.getUserId())) continue;
                boolean win = Long.valueOf(winners.get(0)).equals(userDTO.getUserId());
                Long rivalId = Long.valueOf(win ? losers.get(0) : winners.get(0));
                personalMatchResMap.putIfAbsent(rivalId, new Integer[]{0, 0});
                Integer[] matchRes = personalMatchResMap.get(rivalId);
                matchRes[1]++;
                if (win) matchRes[0]++;
            } else if (winners.size() == 2 && losers.size() == 2) {
                if (!winners.contains(userDTO.getUserId().toString()) &&
                        !losers.contains(userDTO.getUserId().toString())) continue;
                boolean win = winners.contains(userDTO.getUserId().toString());
                Long mateId = Long.valueOf(win
                        ?
                        winners.get(0).equals(userDTO.getUserId().toString()) ? winners.get(1) : winners.get(0)
                        :
                        losers.get(0).equals(userDTO.getUserId().toString()) ? losers.get(1) : losers.get(0));
                personalDoublesMatchResMap.putIfAbsent(mateId, new Integer[]{0, 0});
                Integer[] matchRes = personalDoublesMatchResMap.get(mateId);
                matchRes[1]++;
                if (win) matchRes[0]++;
            } else continue;
        }
        sb.append("【单打胜率】\n");
        for (Long playerId : personalMatchResMap.keySet()) {
            String name = userDAO.queryByUserId(playerId).getName();
            sb.append(name + ": ");
            Integer[] val = personalMatchResMap.get(playerId);
            double rate = (double) val[0] / ((double) val[1]) * 100;
            sb.append(
                    String.format("%d 胜 %d 负 %.2f%%\n", val[0], val[1] - val[0], rate));
        }

        sb.append("【双打胜率】\n");
        for (Long playerId : personalDoublesMatchResMap.keySet()) {
            String name = userDAO.queryByUserId(playerId).getName();
            sb.append(name + ": ");
            Integer[] val = personalDoublesMatchResMap.get(playerId);
            double rate = (double) val[0] / ((double) val[1]) * 100;
            sb.append(
                    String.format("%d 胜 %d 负 %.2f%%\n", val[0], val[1] - val[0], rate));
        }

        textMessage.setData(sb.toString());

        return textMessage;
    }

    @Handler(command = "荣誉")
    public TextMessage honor() {
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("荣誉殿堂");
        StringBuilder sb = new StringBuilder();

        Map<Long, Integer[]> playerHonorMap = new HashMap<>();
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll(season);

        for (BilliardRecordDTO br : brList) {
            if (!gameTypeMap.get(br.getGameType()).equals("决赛")) continue;
            List<String> winners = Arrays.asList(br.getWinnerId().split(","));
            List<String> losers = Arrays.asList(br.getLoserId().split(","));
            for (String wid : winners) {
                Long winner = Long.valueOf(wid);
                playerHonorMap.putIfAbsent(winner, new Integer[]{0, 0});
                Integer[] honorList = playerHonorMap.get(winner);
                honorList[0]++;
            }
            for (String lid : losers) {
                Long loser = Long.valueOf(lid);
                playerHonorMap.putIfAbsent(loser, new Integer[]{0, 0});
                Integer[] honorList = playerHonorMap.get(loser);
                honorList[1]++;
            }
        }
        for (Long playerId : playerHonorMap.keySet()) {
            String name = userDAO.queryByUserId(playerId).getName();
            sb.append(name + ": \n");
            sb.append(StringUtils.repeat("❽", playerHonorMap.get(playerId)[0]));
            sb.append(StringUtils.repeat("○", playerHonorMap.get(playerId)[1]));
            sb.append("\n");
        }

        textMessage.setData(sb.toString());
        return textMessage;
    }

    @Handler(command = "能力")
    public ImageMessage getRadarMap(@Param("选手") String username) throws IOException {
        ImageMessage message = new ImageMessage();
        UserDTO userDTO = userDAO.queryByAlias(username);
        if (userDTO == null) {
            return message;
        }

        double gameCount = 0;
        double winGameCount = 0;
        double unexpectedWinGameCount = 0;
        double unexpectedLoseGameCount = 0;
        double multiGameCount = 0;
        double multiWinGameCount = 0;
        double scoreSum = 0;
        double rivalScoreSum = 0;
        double winScoreDiffSum = 0;

        double allGameCount = 0;
        double allScoreSum = 0;
        double allDiffSum = 0;
        double allUnexpectedResGameCount = 0;
        Long season = Long.valueOf(cacheService.get("billiard", "season", "1"));
        List<BilliardRecordDTO> brList = billiardRecordDAO.queryAll(season);
        for (BilliardRecordDTO br : brList) {
            List<String> winners = Arrays.asList(br.getWinnerId().split(","));
            List<String> losers = Arrays.asList(br.getLoserId().split(","));
            int scoreW = br.getScoreW();
            int scoreL = br.getScoreL();
            allGameCount++;
            allScoreSum += scoreW + scoreL;
            allDiffSum += scoreW > scoreL ? scoreW - scoreL : 0;
            allUnexpectedResGameCount += scoreW == 8 ? 0 : 1;
            if (!winners.contains(userDTO.getUserId().toString()) &&
                    !losers.contains(userDTO.getUserId().toString())) continue;
            gameCount++;
            if (winners.contains(userDTO.getUserId().toString())) {
                //win
                winGameCount++;
                scoreSum += scoreW;
                rivalScoreSum += scoreL;
                winScoreDiffSum += scoreW > scoreL ? scoreW - scoreL : 0;
                unexpectedWinGameCount += scoreW == 8 ? 0 : 1;
                if (winners.size() > 1) {
                    multiGameCount++;
                    multiWinGameCount++;
                }
            } else {
                //lose
                scoreSum += scoreL;
                rivalScoreSum += scoreW;
                unexpectedLoseGameCount += scoreW == 8 ? 0 : 1;
                if (losers.size() > 1)
                    multiGameCount++;
            }
        }

        double max = 0;
        double min = 0;
        //normalization
        TriFunction<Double, Double, Double, Double> normalize = (_val, _max, _min) -> 10 * (_val - _min) / (_max - _min);
        //score
        double score = scoreSum / gameCount;
        double avgScore = allScoreSum / allGameCount / 2;
        max = 8;
        min = 2 * avgScore - 8;
        double normalizedScore = normalize.apply(score, max, min);
        //defence
        double rivalScore = rivalScoreSum / gameCount;
        double normalizedRivalScore = normalize.apply(rivalScore, max, min);
        //press
        double winScoreDiff = winScoreDiffSum / gameCount;
        double avgScoreDiff = allDiffSum / allGameCount / 2;
        max = 2 * avgScoreDiff;
        min = 0;
        double normalizedWinScoreDiff = normalize.apply(winScoreDiff, max, min);
        //luck
        double luckRate = unexpectedWinGameCount / gameCount;
        double avgLuckRate = allUnexpectedResGameCount / allGameCount / 2;
        max = 2 * avgLuckRate;
        min = 0;
        double normalizedLuckP = normalize.apply(luckRate, max, min);
        //ball controll
        double misfortuneRate = unexpectedLoseGameCount / gameCount;
        double normalizedMisfortuneP = normalize.apply(misfortuneRate, min, max);

        if (normalizedScore < 0) normalizedScore = 0;
        if (normalizedRivalScore < 0) normalizedRivalScore = 0;
        if (normalizedWinScoreDiff < 0) normalizedWinScoreDiff = 0;
        if (normalizedLuckP < 0) normalizedLuckP = 0;
        if (normalizedMisfortuneP < 0) normalizedMisfortuneP = 0;

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(10, "outline", "Score");
        dataset.addValue(10, "outline", "Defence");
        dataset.addValue(10, "outline", "Press");
        dataset.addValue(10, "outline", "Luck");
        dataset.addValue(10, "outline", "Ball control");
        dataset.addValue(10, "outline", "Teamwork");
        dataset.addValue(10, "outline", "Winning rate");

        dataset.addValue(5, "AVG", "Score");
        dataset.addValue(5, "AVG", "Defence");
        dataset.addValue(5, "AVG", "Press");
        dataset.addValue(5, "AVG", "Luck");
        dataset.addValue(5, "AVG", "Ball control");
        dataset.addValue(5, "AVG", "Teamwork");
        dataset.addValue(5, "AVG", "Winning rate");

        dataset.addValue(normalizedScore, userDTO.getName(), "Score");
        dataset.addValue(10 - normalizedRivalScore, userDTO.getName(), "Defence");
        dataset.addValue(normalizedWinScoreDiff, userDTO.getName(), "Press");
        dataset.addValue(normalizedLuckP, userDTO.getName(), "Luck");
        dataset.addValue(normalizedMisfortuneP, userDTO.getName(), "Ball control");
        dataset.addValue(multiWinGameCount / multiGameCount * 10, userDTO.getName(), "Teamwork");
        dataset.addValue(winGameCount / gameCount * 10, userDTO.getName(), "Winning rate");

        SpiderWebPlot spiderWebPlot = new SpiderWebPlot(dataset);
        spiderWebPlot.setMaxValue(10);
        spiderWebPlot.setBackgroundPaint(Color.WHITE);
        spiderWebPlot.setSeriesOutlineStroke(dataset.getRowIndex("AVG"), new BasicStroke(2.0F, 1, 1, 1.0F, new float[]{30F, 12F}, 0.0F));
        spiderWebPlot.setSeriesPaint(dataset.getRowIndex("outline"), Color.BLACK);
        spiderWebPlot.setSeriesOutlinePaint(dataset.getRowIndex("outline"), Color.BLACK);
        spiderWebPlot.setSeriesPaint(dataset.getRowIndex("AVG"), Color.BLUE);
        spiderWebPlot.setSeriesOutlinePaint(dataset.getRowIndex("AVG"), Color.BLUE);
        spiderWebPlot.setSeriesPaint(dataset.getRowIndex(userDTO.getName()), Color.RED);
        spiderWebPlot.setSeriesOutlinePaint(dataset.getRowIndex(userDTO.getName()), Color.RED);

        JFreeChart chart = new JFreeChart("Ability Radar Chart", spiderWebPlot);

        ChartUtils.saveChartAsJPEG(new File("./go-cqhttp/data/images/cache/chart.jpg"), chart, 600,
                600);
        message.setUrl("cache/chart.jpg");

        return message;
    }

}

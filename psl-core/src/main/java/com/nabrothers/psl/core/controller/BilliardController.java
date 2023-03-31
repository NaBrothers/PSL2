package com.nabrothers.psl.core.controller;

import com.nabrothers.psl.core.dao.BilliardRecordDAO;
import com.nabrothers.psl.core.dao.UserDAO;
import com.nabrothers.psl.core.dto.BilliardRecordDTO;
import com.nabrothers.psl.core.dto.UserDTO;
import com.nabrothers.psl.sdk.annotation.Handler;
import com.nabrothers.psl.sdk.annotation.Param;
import com.nabrothers.psl.sdk.message.TextMessage;
import com.nabrothers.psl.sdk.service.MessageService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.*;

@Component
@Handler(command = "台球")
public class BilliardController {
    @Resource
    private MessageService messageService;

    @Resource
    private UserDAO userDAO;

    @Resource
    private BilliardRecordDAO billiardRecordDAO;

    @Handler(info = "GBL积分榜")
    public TextMessage billiard() {
        TextMessage textMessage = new TextMessage();
        textMessage.setTitle("Geeks Billiard League");

        return textMessage;
    }

    @Handler(command = "记录")
    public String billiardRecord(@Param("比赛类型") String type, @Param("胜者") String winner, @Param("胜者得分") String scoreW,
            @Param("败者") String loser, @Param("败者得分") String scoreL) {
        Map<String, Integer> typeMap = new HashMap<>();
        typeMap.put("小组赛", 0);
        typeMap.put("胜者组", 1);
        typeMap.put("败者组", 2);
        typeMap.put("决赛", 3);

        if (!typeMap.containsKey(type)) {
            return "参数错误";
        }

        if (Integer.valueOf(scoreW) < 0 || Integer.valueOf(scoreL) < 0) {
            return "参数错误";
        }

        String winnerid = "";
        String loserid = "";
        String[] winnerArray = winner.split(",|，");
        String[] loserArray = loser.split(",|，");
        for(int i = 0; i < winnerArray.length; i++){
            UserDTO winnerDTO = userDAO.queryByAlias(winnerArray[i]);
            if(winnerDTO == null){
                return "参数错误";
            }
            winnerid += String.valueOf(winnerDTO.getUserId()) + ",";
        }

        for(int i = 0; i < loserArray.length; i++){
            UserDTO loserDTO = userDAO.queryByAlias(loserArray[i]);
            if(loserDTO == null){
                return "参数错误";
            }
            loserid += String.valueOf(loserDTO.getUserId()) + ",";
        }

        BilliardRecordDTO billiardRecordDTO = new BilliardRecordDTO();
        billiardRecordDTO.setGameType(typeMap.get(type));
        billiardRecordDTO.setWinnerId(winnerid);
        billiardRecordDTO.setLoserId(loserid);
        billiardRecordDTO.setScoreWinner(Integer.valueOf(scoreW));
        billiardRecordDTO.setScoreLoser(Integer.valueOf(scoreL));

        billiardRecordDAO.insert(billiardRecordDTO);

        return "记录成功";
    }
}

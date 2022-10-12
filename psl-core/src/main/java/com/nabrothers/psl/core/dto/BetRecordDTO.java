package com.nabrothers.psl.core.dto;

import lombok.Data;

@Data
public class BetRecordDTO {
    private Long id;
    private Long userId;
    private Long matchId;
    private Double win;
    private Double draw;
    private Double lose;
    private Integer expect;
    private Long amount;
    private Integer result;
    private Long updateTime;
    private Long createTime;
}

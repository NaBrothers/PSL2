package com.nabrothers.psl.core.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BilliardRecordDTO {
    private Long id;
    private Long gameId;
    private String winnerId;
    private String loserId;
    private Integer scoreW;
    private Integer scoreL;
    private Integer gameType;
    private String date;
    private Date updateTime;
    private Date createTime;
}

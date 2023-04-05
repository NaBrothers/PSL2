package com.nabrothers.psl.core.dto;

import lombok.Data;

@Data
public class BilliardRecordDTO {
    private String winnerId;
    private String loserId;
    private Integer scoreW;
    private Integer scoreL;
    private Integer gameType;

}

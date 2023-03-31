package com.nabrothers.psl.core.dto;

import lombok.Data;

@Data
public class BilliardRecordDTO {
    private Integer gameType;
    private String winnerId;
    private String loserId;
    private Integer scoreWinner;
    private Integer scoreLoser;

}

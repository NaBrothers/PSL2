package com.nabrothers.psl.core.dto;

import lombok.Data;

@Data
public class BilliardGameDTO {
    private Long id;
    private String name;
    private String location;
    private String players;
    private String date;
    private String remark;
}

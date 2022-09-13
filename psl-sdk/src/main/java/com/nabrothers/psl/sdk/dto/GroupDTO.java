package com.nabrothers.psl.sdk.dto;

import lombok.Data;

@Data
public class GroupDTO {
    private Long id;
    private Integer level;
    private Long createTime;
    private String memo;
    private String name;
    private Long memberCount;
    private Long maxMemberCount;
}

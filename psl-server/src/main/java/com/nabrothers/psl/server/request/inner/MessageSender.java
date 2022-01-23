package com.nabrothers.psl.server.request.inner;

import lombok.Data;

@Data
public class MessageSender {
    private Long user_id;
    private String sex;
    private String nickname;
    private Long age;

    // 以下字段只有group才有：
    private String area;
    private String role;
    private String level;
    private String title;
    private String card;
}

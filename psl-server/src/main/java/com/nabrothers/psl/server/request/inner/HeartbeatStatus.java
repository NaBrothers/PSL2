package com.nabrothers.psl.server.request.inner;

import lombok.Data;

@Data
public class HeartbeatStatus {
    private Stat stat;
    private Boolean online;
    private Boolean app_enabled;
    private Boolean good;
    private Boolean app_good;
    private Boolean app_initialized;

    @Data
    private class Stat {
        private Long packet_lost;
        private Long packet_sent;
        private Long message_received;
        private Long disconnect_times;
        private Long message_sent;
        private Long last_message_time;
        private Long lost_times;
        private Long packet_received;
    }
}
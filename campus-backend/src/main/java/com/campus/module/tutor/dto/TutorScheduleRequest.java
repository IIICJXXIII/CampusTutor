package com.campus.module.tutor.dto;

import lombok.Data;
import java.util.List;

/**
 * 教员时间配置请求
 */
@Data
public class TutorScheduleRequest {

    /**
     * 时间段列表
     */
    private List<ScheduleItem> schedules;

    @Data
    public static class ScheduleItem {
        /**
         * 星期几：1-7
         */
        private Integer dayOfWeek;

        /**
         * 开始时间(HH:mm)
         */
        private String startTime;

        /**
         * 结束时间(HH:mm)
         */
        private String endTime;

        /**
         * 是否可用：0否 1是
         */
        private Integer available;
    }
}

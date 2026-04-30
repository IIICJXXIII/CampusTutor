package com.campus.module.demand.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 申请列表视图对象（含教师用户信息）
 */
@Data
public class TutorApplicationVO {

    private Long id;
    private Long demandId;
    private Long tutorId;
    private Long tutorProfileId;
    private Integer totalHours;
    private String remark;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 教师用户信息
    private String tutorNickname;
    private String tutorAvatar;
    private String tutorPhone;
}

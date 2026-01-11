package com.campus.module.behavior.dto;

import lombok.Data;

/**
 * 行为记录请求 DTO
 */
@Data
public class BehaviorRecordRequest {

    /**
     * 目标ID（教员ID）
     */
    private Long targetId;

    /**
     * 行为类型：1-查看详情, 3-收藏, 4-发起聊天
     */
    private Integer actionType;

    /**
     * 停留时长(秒)，仅查看详情时需要
     */
    private Integer duration;
}

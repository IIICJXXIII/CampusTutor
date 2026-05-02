package com.campus.module.behavior.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户行为日志实体
 * 映射 user_action_log 表
 */
@Data
@TableName("user_action_log")
public class UserActionLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（家长）
     */
    private Long userId;

    /**
     * 目标ID（教员ID或搜索关键词ID）
     */
    private Long targetId;

    /**
     * 行为类型：1-查看教员详情, 2-搜索科目, 3-收藏教员, 4-发起聊天, 5-下单
     */
    private Integer actionType;

    /**
     * 停留时长(秒)
     */
    private Integer duration;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Integer getActionType() { return actionType; }
    public void setActionType(Integer actionType) { this.actionType = actionType; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}

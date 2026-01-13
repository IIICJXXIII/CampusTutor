package com.campus.module.teaching.service;

import com.campus.module.teaching.dto.CheckInRequest;
import com.campus.module.teaching.dto.TeachingRecordDTO;

import java.util.List;

/**
 * 课时打卡服务接口
 */
public interface TeachingRecordService {

    /**
     * 教师打卡上课
     * @param tutorId 教师用户ID
     * @param request 打卡请求
     * @return 课时记录ID
     */
    Long checkIn(Long tutorId, CheckInRequest request);

    /**
     * 教师打卡下课
     * @param tutorId 教师用户ID
     * @param recordId 课时记录ID
     * @param contentSummary 教学内容摘要
     * @param homeworkAssigned 布置作业
     */
    void checkOut(Long tutorId, Long recordId, String contentSummary, String homeworkAssigned);

    /**
     * 家长确认课时
     * @param parentId 家长用户ID
     * @param recordId 课时记录ID
     */
    void confirmByParent(Long parentId, Long recordId);

    /**
     * 家长申诉课时
     * @param parentId 家长用户ID
     * @param recordId 课时记录ID
     * @param reason 申诉原因
     */
    void disputeByParent(Long parentId, Long recordId, String reason);

    /**
     * 获取订单的所有课时记录
     * @param orderId 订单ID
     * @return 课时记录列表
     */
    List<TeachingRecordDTO> getRecordsByOrderId(Long orderId);

    /**
     * 获取课时记录详情
     * @param recordId 记录ID
     * @return 课时记录
     */
    TeachingRecordDTO getRecordById(Long recordId);

    /**
     * 根据用户ID获取其相关的所有课时记录
     * @param userId 用户ID
     * @param role 用户角色 (1-教员, 2-家长)
     * @return 课时记录列表
     */
    List<TeachingRecordDTO> getRecordsByUserId(Long userId, Integer role);
    
    /**
     * 更新课时进度
     * @param tutorId 教师用户ID
     * @param recordId 课时记录ID
     * @param progress 进度值 (0-100)
     * @param notes 备注
     */
    void updateLessonProgress(Long tutorId, Long recordId, Integer progress, String notes);
    
    /**
     * 获取课程统计信息
     * @param orderId 订单ID
     * @return 统计信息
     */
    java.util.Map<String, Object> getCourseStatistics(Long orderId);
}

package com.campus.module.parent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.parent.dto.StudentRequest;
import com.campus.module.parent.entity.ParentStudent;

import java.util.List;

/**
 * 家长学生Service
 */
public interface ParentStudentService extends IService<ParentStudent> {

    /**
     * 添加学生
     * @param parentId 家长ID
     * @param request 学生信息
     * @return 学生ID
     */
    Long addStudent(Long parentId, StudentRequest request);

    /**
     * 更新学生信息
     * @param parentId 家长ID
     * @param request 学生信息
     */
    void updateStudent(Long parentId, StudentRequest request);

    /**
     * 删除学生
     * @param parentId 家长ID
     * @param studentId 学生ID
     */
    void deleteStudent(Long parentId, Long studentId);

    /**
     * 获取家长的所有学生
     * @param parentId 家长ID
     * @return 学生列表
     */
    List<ParentStudent> listByParentId(Long parentId);
}

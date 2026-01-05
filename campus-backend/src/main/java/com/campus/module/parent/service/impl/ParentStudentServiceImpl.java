package com.campus.module.parent.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;
import com.campus.module.parent.dto.StudentRequest;
import com.campus.module.parent.entity.ParentStudent;
import com.campus.module.parent.mapper.ParentStudentMapper;
import com.campus.module.parent.service.ParentStudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 家长学生Service实现
 */
@Service
public class ParentStudentServiceImpl extends ServiceImpl<ParentStudentMapper, ParentStudent> 
        implements ParentStudentService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addStudent(Long parentId, StudentRequest request) {
        ParentStudent student = new ParentStudent();
        student.setParentId(parentId);
        student.setStudentName(request.getStudentName());
        student.setGender(request.getGender());
        student.setGrade(request.getGrade());
        student.setSchoolName(request.getSchoolName());
        if (request.getWeakSubjects() != null) {
            student.setWeakSubjects(JSONUtil.toJsonStr(request.getWeakSubjects()));
        }
        student.setStudyDesc(request.getStudyDesc());
        save(student);
        return student.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudent(Long parentId, StudentRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "学生ID不能为空");
        }
        ParentStudent student = getById(request.getId());
        if (student == null || !student.getParentId().equals(parentId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "学生不存在或无权限");
        }

        student.setStudentName(request.getStudentName());
        student.setGender(request.getGender());
        student.setGrade(request.getGrade());
        student.setSchoolName(request.getSchoolName());
        if (request.getWeakSubjects() != null) {
            student.setWeakSubjects(JSONUtil.toJsonStr(request.getWeakSubjects()));
        }
        student.setStudyDesc(request.getStudyDesc());
        updateById(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(Long parentId, Long studentId) {
        ParentStudent student = getById(studentId);
        if (student == null || !student.getParentId().equals(parentId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "学生不存在或无权限");
        }
        removeById(studentId);
    }

    @Override
    public List<ParentStudent> listByParentId(Long parentId) {
        return list(new LambdaQueryWrapper<ParentStudent>()
                .eq(ParentStudent::getParentId, parentId)
                .orderByDesc(ParentStudent::getCreateTime));
    }
}

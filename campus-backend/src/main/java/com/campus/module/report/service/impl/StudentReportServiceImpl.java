package com.campus.module.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.module.report.entity.StudentReport;
import com.campus.module.report.mapper.StudentReportMapper;
import com.campus.module.report.service.StudentReportService;
import org.springframework.stereotype.Service;

@Service
public class StudentReportServiceImpl extends ServiceImpl<StudentReportMapper, StudentReport>
        implements StudentReportService {

    @Override
    public IPage<StudentReport> listByStudentId(Long studentId, Integer reportType, Integer page, Integer size) {
        Page<StudentReport> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<StudentReport> wrapper = new LambdaQueryWrapper<StudentReport>()
                .eq(StudentReport::getStudentId, studentId);
        if (reportType != null) {
            wrapper.eq(StudentReport::getReportType, reportType);
        }
        wrapper.orderByDesc(StudentReport::getCreateTime);
        return page(pageParam, wrapper);
    }

    @Override
    public StudentReport createReport(StudentReport report) {
        save(report);
        return report;
    }

    @Override
    public StudentReport getByOrderId(Long orderId) {
        return getOne(new LambdaQueryWrapper<StudentReport>()
                .eq(StudentReport::getOrderId, orderId)
                .last("LIMIT 1"));
    }
}

package com.campus.module.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.report.entity.StudentReport;

public interface StudentReportService extends IService<StudentReport> {

    IPage<StudentReport> listByStudentId(Long studentId, Integer reportType, Integer page, Integer size);

    StudentReport createReport(StudentReport report);

    StudentReport getByOrderId(Long orderId);
}

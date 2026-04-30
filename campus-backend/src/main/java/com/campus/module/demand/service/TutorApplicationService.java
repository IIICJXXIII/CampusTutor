package com.campus.module.demand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.demand.dto.TutorApplicationVO;
import com.campus.module.demand.entity.TutorApplication;

import java.util.List;

public interface TutorApplicationService extends IService<TutorApplication> {

    Long applyForDemand(Long tutorId, Long demandId, Integer totalHours, String remark);

    IPage<TutorApplicationVO> listByDemandId(Long demandId, Integer page, Integer size);

    List<TutorApplicationVO> listByDemandId(Long demandId);

    IPage<TutorApplication> listByTutorId(Long tutorId, Integer status, Integer page, Integer size);

    void acceptApplication(Long parentId, Long applicationId);

    void rejectApplication(Long parentId, Long applicationId, String reason);
}

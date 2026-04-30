package com.campus.module.demand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;
import com.campus.module.demand.dto.TutorApplicationVO;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.demand.entity.TutorApplication;
import com.campus.module.demand.mapper.DemandPostMapper;
import com.campus.module.demand.mapper.TutorApplicationMapper;
import com.campus.module.demand.service.TutorApplicationService;
import com.campus.module.order.entity.CourseOrder;
import com.campus.module.order.service.CourseOrderService;
import com.campus.module.tutor.entity.TutorProfile;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorApplicationServiceImpl extends ServiceImpl<TutorApplicationMapper, TutorApplication>
        implements TutorApplicationService {

    private final DemandPostMapper demandPostMapper;
    private final TutorProfileMapper tutorProfileMapper;
    private final SysUserMapper sysUserMapper;

    @Lazy
    private final CourseOrderService orderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyForDemand(Long tutorId, Long demandId, Integer totalHours, String remark) {
        DemandPost demand = demandPostMapper.selectById(demandId);
        if (demand == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求不存在");
        }
        if (demand.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求已下架或已被接单");
        }

        LambdaQueryWrapper<TutorProfile> profileWrapper = new LambdaQueryWrapper<TutorProfile>()
                .eq(TutorProfile::getUserId, tutorId)
                .eq(TutorProfile::getCertStatus, 2);
        TutorProfile tutorProfile = tutorProfileMapper.selectOne(profileWrapper);
        if (tutorProfile == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "教员未认证或档案不存在");
        }

        LambdaQueryWrapper<TutorApplication> existWrapper = new LambdaQueryWrapper<TutorApplication>()
                .eq(TutorApplication::getDemandId, demandId)
                .eq(TutorApplication::getTutorId, tutorId)
                .ne(TutorApplication::getStatus, 2);
        long existCount = count(existWrapper);
        if (existCount > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "您已申请过该需求，请等待家长审核");
        }

        TutorApplication application = new TutorApplication();
        application.setDemandId(demandId);
        application.setTutorId(tutorId);
        application.setTutorProfileId(tutorProfile.getId());
        application.setTotalHours(totalHours != null ? totalHours : 10);
        application.setRemark(remark);
        application.setStatus(0);
        save(application);

        // 创建关联订单，让教师可以在"我的订单"中看到申请并取消
        com.campus.module.order.entity.CourseOrder order = new com.campus.module.order.entity.CourseOrder();
        order.setOrderNo("APP" + System.currentTimeMillis() + cn.hutool.core.util.IdUtil.simpleUUID().substring(0, 6).toUpperCase());
        order.setParentId(demand.getPublisherId());
        order.setStudentId(demand.getStudentId());
        order.setTutorId(tutorId);
        order.setTutorProfileId(tutorProfile.getId());
        order.setDemandId(demandId);
        order.setApplicationId(application.getId());
        order.setSubject(demand.getSubject());
        order.setGrade(demand.getGrade());
        order.setTeachMode(demand.getTeachMode() == 3 ? 1 : demand.getTeachMode());
        order.setUnitPrice(demand.getExpectPrice());
        order.setTotalHours(totalHours != null ? totalHours : 10);
        java.math.BigDecimal totalAmount = demand.getExpectPrice().multiply(new java.math.BigDecimal(order.getTotalHours()));
        order.setTotalAmount(totalAmount);
        order.setServiceFee(java.math.BigDecimal.ZERO);
        order.setTutorAmount(totalAmount);
        order.setUsedHours(0);
        order.setStatus(-1);
        order.setRemark(remark);
        order.setLongitude(demand.getLongitude());
        order.setLatitude(demand.getLatitude());
        order.setAddress(demand.getAddress());
        orderService.save(order);

        log.info("[教师申请] 教师 {} 申请需求 {}, 申请ID: {}, 订单ID: {}", tutorId, demandId, application.getId(), order.getId());
        return application.getId();
    }

    @Override
    public IPage<TutorApplicationVO> listByDemandId(Long demandId, Integer page, Integer size) {
        Page<TutorApplication> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TutorApplication> wrapper = new LambdaQueryWrapper<TutorApplication>()
                .eq(TutorApplication::getDemandId, demandId)
                .eq(TutorApplication::getStatus, 0)
                .orderByDesc(TutorApplication::getCreateTime);
        IPage<TutorApplication> pageResult = page(pageParam, wrapper);

        Page<TutorApplicationVO> voPage = new Page<>(page, size);
        voPage.setTotal(pageResult.getTotal());
        voPage.setRecords(enrichWithUserInfo(pageResult.getRecords()));
        return voPage;
    }

    @Override
    public List<TutorApplicationVO> listByDemandId(Long demandId) {
        LambdaQueryWrapper<TutorApplication> wrapper = new LambdaQueryWrapper<TutorApplication>()
                .eq(TutorApplication::getDemandId, demandId)
                .eq(TutorApplication::getStatus, 0)
                .orderByDesc(TutorApplication::getCreateTime);
        List<TutorApplication> applications = list(wrapper);
        return enrichWithUserInfo(applications);
    }

    private List<TutorApplicationVO> enrichWithUserInfo(List<TutorApplication> applications) {
        List<TutorApplicationVO> vos = new java.util.ArrayList<>();
        for (TutorApplication app : applications) {
            TutorApplicationVO vo = new TutorApplicationVO();
            vo.setId(app.getId());
            vo.setDemandId(app.getDemandId());
            vo.setTutorId(app.getTutorId());
            vo.setTutorProfileId(app.getTutorProfileId());
            vo.setTotalHours(app.getTotalHours());
            vo.setRemark(app.getRemark());
            vo.setStatus(app.getStatus());
            vo.setRejectReason(app.getRejectReason());
            vo.setCreateTime(app.getCreateTime());
            vo.setUpdateTime(app.getUpdateTime());

            SysUser user = sysUserMapper.selectById(app.getTutorId());
            if (user != null) {
                vo.setTutorNickname(user.getNickname());
                vo.setTutorAvatar(user.getAvatarUrl());
                vo.setTutorPhone(user.getUsername());
            }
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public IPage<TutorApplication> listByTutorId(Long tutorId, Integer status, Integer page, Integer size) {
        Page<TutorApplication> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TutorApplication> wrapper = new LambdaQueryWrapper<TutorApplication>()
                .eq(TutorApplication::getTutorId, tutorId);
        if (status != null) {
            wrapper.eq(TutorApplication::getStatus, status);
        }
        wrapper.orderByDesc(TutorApplication::getCreateTime);
        return page(pageParam, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptApplication(Long parentId, Long applicationId) {
        TutorApplication application = getById(applicationId);
        if (application == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "申请不存在");
        }
        if (application.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该申请已被处理");
        }

        DemandPost demand = demandPostMapper.selectById(application.getDemandId());
        if (demand == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求不存在");
        }
        if (!demand.getPublisherId().equals(parentId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此申请");
        }
        if (demand.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "需求状态不正确，无法接受申请");
        }

        application.setStatus(1);
        updateById(application);

        demand.setMatchedTutorId(application.getTutorId());
        demand.setStatus(2);
        demandPostMapper.updateById(demand);

        // 更新已有的申请订单状态（由applyForDemand创建的订单）
        LambdaQueryWrapper<com.campus.module.order.entity.CourseOrder> orderWrapper = new LambdaQueryWrapper<com.campus.module.order.entity.CourseOrder>()
                .eq(com.campus.module.order.entity.CourseOrder::getApplicationId, applicationId);
        com.campus.module.order.entity.CourseOrder existingOrder = orderService.getOne(orderWrapper);
        if (existingOrder != null) {
            existingOrder.setStatus(0);
            existingOrder.setRemark(application.getRemark());
            existingOrder.setTotalHours(application.getTotalHours() != null ? application.getTotalHours() : 10);
            java.math.BigDecimal totalAmount = demand.getExpectPrice()
                    .multiply(new java.math.BigDecimal(existingOrder.getTotalHours()));
            existingOrder.setTotalAmount(totalAmount);
            orderService.updateById(existingOrder);
            log.info("[家长接受申请] 更新已有订单 {}, 状态变更为待支付", existingOrder.getId());
        } else {
            // 兼容旧数据：没有关联订单时创建新订单
            com.campus.module.order.dto.AcceptDemandRequest acceptRequest = new com.campus.module.order.dto.AcceptDemandRequest();
            acceptRequest.setDemandId(demand.getId());
            acceptRequest.setTotalHours(application.getTotalHours() != null ? application.getTotalHours() : 10);
            acceptRequest.setRemark(application.getRemark());
            Long newOrderId = orderService.acceptDemand(application.getTutorId(), acceptRequest);
            log.info("[家长接受申请] 创建新订单 {}", newOrderId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectApplication(Long parentId, Long applicationId, String reason) {
        TutorApplication application = getById(applicationId);
        if (application == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "申请不存在");
        }
        if (application.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该申请已被处理");
        }

        DemandPost demand = demandPostMapper.selectById(application.getDemandId());
        if (demand == null || !demand.getPublisherId().equals(parentId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "无权操作此申请");
        }

        application.setStatus(2);
        application.setRejectReason(reason);
        updateById(application);

        log.info("[家长拒绝申请] 家长 {} 拒绝教师 {} 的申请, 原因: {}", parentId, application.getTutorId(), reason);
    }
}

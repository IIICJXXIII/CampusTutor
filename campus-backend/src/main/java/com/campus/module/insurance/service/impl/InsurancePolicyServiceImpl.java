package com.campus.module.insurance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.module.insurance.entity.InsurancePolicy;
import com.campus.module.insurance.mapper.InsurancePolicyMapper;
import com.campus.module.insurance.service.InsurancePolicyService;
import com.campus.module.order.entity.CourseOrder;
import com.campus.module.order.mapper.CourseOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InsurancePolicyServiceImpl extends ServiceImpl<InsurancePolicyMapper, InsurancePolicy>
        implements InsurancePolicyService {

    private final CourseOrderMapper courseOrderMapper;

    @Override
    public IPage<InsurancePolicy> listByUserId(Long userId, Integer status, Integer page, Integer size) {
        Page<InsurancePolicy> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<InsurancePolicy> wrapper = new LambdaQueryWrapper<InsurancePolicy>()
                .inSql(InsurancePolicy::getOrderId,
                        "SELECT id FROM course_order WHERE parent_id = " + userId
                                + " OR tutor_id = " + userId);
        if (status != null) {
            wrapper.eq(InsurancePolicy::getStatus, status);
        }
        wrapper.orderByDesc(InsurancePolicy::getCreateTime);
        IPage<InsurancePolicy> result = page(pageParam, wrapper);
        result.getRecords().forEach(this::fillOrderNo);
        return result;
    }

    @Override
    public InsurancePolicy getByOrderId(Long orderId) {
        InsurancePolicy policy = getOne(new LambdaQueryWrapper<InsurancePolicy>()
                .eq(InsurancePolicy::getOrderId, orderId)
                .last("LIMIT 1"));
        if (policy != null) {
            fillOrderNo(policy);
        }
        return policy;
    }

    @Override
    public InsurancePolicy createPolicy(InsurancePolicy policy) {
        if (policy.getPolicyNo() == null) {
            policy.setPolicyNo("INS" + System.currentTimeMillis());
        }
        if (policy.getProvider() == null) {
            policy.setProvider("PingAn");
        }
        if (policy.getStatus() == null) {
            policy.setStatus(1);
        }
        save(policy);
        return policy;
    }

    private void fillOrderNo(InsurancePolicy policy) {
        CourseOrder order = courseOrderMapper.selectById(policy.getOrderId());
        if (order != null) {
            policy.setOrderNo(order.getOrderNo());
        }
    }
}

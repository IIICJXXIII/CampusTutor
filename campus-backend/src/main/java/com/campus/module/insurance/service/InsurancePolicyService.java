package com.campus.module.insurance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.insurance.entity.InsurancePolicy;

public interface InsurancePolicyService extends IService<InsurancePolicy> {

    IPage<InsurancePolicy> listByUserId(Long userId, Integer status, Integer page, Integer size);

    InsurancePolicy getByOrderId(Long orderId);

    InsurancePolicy createPolicy(InsurancePolicy policy);
}

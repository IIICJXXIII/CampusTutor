package com.campus.module.insurance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("insurance_policy")
public class InsurancePolicy {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String policyNo;

    private String provider;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String orderNo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getPolicyNo() { return policyNo; }
    public void setPolicyNo(String policyNo) { this.policyNo = policyNo; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
}

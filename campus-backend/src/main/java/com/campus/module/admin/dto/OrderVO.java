package com.campus.module.admin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表 VO
 */
@Data
public class OrderVO {

    private Long id;

    private String orderNo;

    /** 科目 */
    private String subject;

    /** 订单金额 */
    private BigDecimal amount;

    /** 订单状态: 0-待支付, 1-已支付, 2-托管中, 3-进行中, 4-已完成, 5-已取消 */
    private Integer status;

    /** 家长ID */
    private Long parentId;

    /** 家长昵称 */
    private String parentName;

    /** 教师ID */
    private Long tutorId;

    /** 教师昵称 */
    private String tutorName;

    private LocalDateTime createTime;
}

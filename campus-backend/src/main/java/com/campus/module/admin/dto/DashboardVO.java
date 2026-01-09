package com.campus.module.admin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 仪表盘统计数据 VO
 */
@Data
public class DashboardVO {

    /** 用户总数 */
    private Integer totalUsers;

    /** 教师总数 */
    private Integer totalTutors;

    /** 家长总数 */
    private Integer totalParents;

    /** 订单总数 */
    private Integer totalOrders;

    /** 总交易额 */
    private BigDecimal totalRevenue;

    /** 待审核认证数 */
    private Integer pendingAudit;

    /** 待处理订单数 */
    private Integer pendingOrders;

    /** 待确认课时数 */
    private Integer pendingLessons;

    /** 待处理退款数 */
    private Integer pendingRefunds;

    /** 最新注册用户 */
    private List<UserVO> recentUsers;

    /** 最新订单 */
    private List<OrderVO> recentOrders;

    /** 教师数量(饼图) */
    private Integer tutorCount;

    /** 家长数量(饼图) */
    private Integer parentCount;
}

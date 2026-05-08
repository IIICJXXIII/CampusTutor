package com.campus.module.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.order.dto.AcceptDemandRequest;
import com.campus.module.order.dto.CreateOrderRequest;
import com.campus.module.order.dto.PayOrderRequest;
import com.campus.module.order.entity.CourseOrder;
import java.util.Map;

/**
 * 课程订单Service
 */
public interface CourseOrderService extends IService<CourseOrder> {

    /**
     * 创建订单
     * 
     * @param parentId 家长ID
     * @param request  订单信息
     * @return 订单ID
     */
    Long createOrder(Long parentId, CreateOrderRequest request);

    /**
     * 支付订单
     * 
     * @param userId  用户ID
     * @param request 支付信息
     * @return 支付参数
     */
    Map<String, String> payOrder(Long userId, PayOrderRequest request);

    /**
     * 取消订单
     * 
     * @param userId  用户ID
     * @param orderId 订单ID
     * @param reason  取消原因
     */
    void cancelOrder(Long userId, Long orderId, String reason);

    void confirmStart(Long parentId, Long orderId);

    /**
     * 完成订单
     * 
     * @param tutorId 教员ID
     * @param orderId 订单ID
     */
    void completeOrder(Long tutorId, Long orderId);

    /**
     * 获取家长订单列表
     * 
     * @param parentId 家长ID
     * @param status   状态筛选
     * @param page     页码
     * @param size     每页数量
     * @return 分页结果
     */
    IPage<CourseOrder> listParentOrders(Long parentId, Integer status, Integer page, Integer size);

    /**
     * 获取教员订单列表
     * 
     * @param tutorId 教员ID
     * @param status  状态筛选
     * @param page    页码
     * @param size    每页数量
     * @return 分页结果
     */
    IPage<CourseOrder> listTutorOrders(Long tutorId, Integer status, Integer page, Integer size);

    /**
     * 教师接单（基于需求帖创建订单）
     * 
     * @param tutorId 教员ID
     * @param request 接单请求
     * @return 订单ID
     */
    Long acceptDemand(Long tutorId, AcceptDemandRequest request);

    /**
     * 家长确认订单
     * 
     * @param parentId 家长ID
     * @param orderId  订单ID
     */
    void confirmOrder(Long parentId, Long orderId);

    void parentRejectOrder(Long parentId, Long orderId, String reason);

    void tutorConfirmOrder(Long tutorId, Long orderId);

    /**
     * 教师拒绝预约订单（家长直接预约场景）
     *
     * @param tutorId 教师用户ID
     * @param orderId 订单ID
     * @param reason  拒绝原因
     */
    void tutorRejectOrder(Long tutorId, Long orderId, String reason);

    /**
     * 申请退款
     *
     * @param userId       用户ID
     * @param orderId      订单ID
     * @param refundAmount 退款金额
     * @param reason       退款原因
     * @return 退款单号
     */
    String applyRefund(Long userId, Long orderId, java.math.BigDecimal refundAmount, String reason);

    /**
     * 按课时逐节结算：家长确认课时后，释放对应比例资金给教师
     *
     * @param order  订单
     * @param record 课时记录
     */
    void releasePerLessonPayment(CourseOrder order, com.campus.module.teaching.entity.TeachingRecord record);

}

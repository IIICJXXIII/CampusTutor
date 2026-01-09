package com.campus.module.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.order.dto.AcceptDemandRequest;
import com.campus.module.order.dto.CreateOrderRequest;
import com.campus.module.order.dto.PayOrderRequest;
import com.campus.module.order.entity.CourseOrder;

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
     */
    void payOrder(Long userId, PayOrderRequest request);

    /**
     * 取消订单
     * 
     * @param userId  用户ID
     * @param orderId 订单ID
     * @param reason  取消原因
     */
    void cancelOrder(Long userId, Long orderId, String reason);

    /**
     * 确认开课
     * 
     * @param tutorId 教员ID
     * @param orderId 订单ID
     */
    void confirmStart(Long tutorId, Long orderId);

    /**
     * 完成订单
     * 
     * @param orderId 订单ID
     */
    void completeOrder(Long orderId);

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
}

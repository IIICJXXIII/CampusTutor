package com.campus.module.demand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.demand.dto.DemandPostRequest;
import com.campus.module.demand.entity.DemandPost;

import java.util.List;

/**
 * 需求帖子Service
 */
public interface DemandPostService extends IService<DemandPost> {

    /**
     * 发布需求
     * @param publisherId 发布者ID
     * @param request 需求信息
     * @return 需求ID
     */
    Long publishDemand(Long publisherId, DemandPostRequest request);

    /**
     * 更新需求
     * @param publisherId 发布者ID
     * @param request 需求信息
     */
    void updateDemand(Long publisherId, DemandPostRequest request);

    /**
     * 上架/下架需求
     * @param publisherId 发布者ID
     * @param demandId 需求ID
     * @param status 状态：0下架 1上架
     */
    void changeStatus(Long publisherId, Long demandId, Integer status);

    /**
     * 删除需求
     * @param publisherId 发布者ID
     * @param demandId 需求ID
     */
    void deleteDemand(Long publisherId, Long demandId);

    /**
     * 获取我发布的需求列表
     * @param publisherId 发布者ID
     * @return 需求列表
     */
    List<DemandPost> listMyDemands(Long publisherId);

    /**
     * 分页查询需求(公开列表)
     * @param subject 科目筛选
     * @param grade 年级筛选
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<DemandPost> pageList(String subject, String grade, Integer page, Integer size);

    /**
     * 按距离搜索附近需求
     * @param longitude 经度
     * @param latitude 纬度
     * @param radiusKm 半径(公里)
     * @return 需求列表
     */
    List<DemandPost> searchNearby(Double longitude, Double latitude, Double radiusKm);

    /**
     * 获取带有匹配度的需求列表（教师端专用）
     * @param tutorId 教师ID
     * @param subject 科目筛选
     * @param grade 年级筛选
     * @param longitude 经度
     * @param latitude 纬度
     * @param page 页码
     * @param size 每页数量
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @return 分页结果
     */
    IPage<DemandPost> pageListWithMatchScore(Long tutorId, String subject, String grade, Double longitude, Double latitude, Integer page, Integer size, String sortBy, String sortOrder);

    /**
     * 教师接单匹配
     * @param tutorId 教师ID
     * @param demandId 需求ID
     * @return 订单ID
     */
    Long matchDemand(Long tutorId, Long demandId);
}

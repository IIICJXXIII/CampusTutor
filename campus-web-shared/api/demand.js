/**
 * 需求相关 API (共享模块)
 */
import request from './request';

/**
 * 创建需求
 * @param {Object} data - 需求信息
 */
export function createDemand(data) {
  return request.post('/demand/publish', data);
}

/**
 * 更新需求
 * @param {Object} data - 需求信息
 */
export function updateDemand(data) {
  return request.put('/demand/update', data);
}

/**
 * 上架需求
 * @param {number} id - 需求ID
 */
export function publishDemand(id) {
  return request.post(`/demand/${id}/online`);
}

/**
 * 下架需求
 * @param {number} id - 需求ID
 */
export function withdrawDemand(id) {
  return request.post(`/demand/${id}/offline`);
}

/**
 * 删除需求
 * @param {number} id - 需求ID
 */
export function deleteDemand(id) {
  return request.delete(`/demand/${id}`);
}

/**
 * 获取我发布的需求
 * @param {Object} params - 分页参数
 */
export function getMyDemands(params) {
  return request.get('/demand/my', { params });
}

/**
 * 获取需求详情
 * @param {number} id - 需求ID
 */
export function getDemandDetail(id) {
  return request.get(`/demand/${id}`);
}

/**
 * 获取公开需求列表 (教师查看)
 * @param {Object} params - 筛选参数
 */
export function getDemandList(params) {
  return request.get('/demand/list', { params });
}

/**
 * 获取附近需求 (LBS)
 * @param {Object} params - { longitude, latitude, radius, page, size }
 */
export function getNearbyDemands(params) {
  return request.get('/demand/nearby', { params });
}

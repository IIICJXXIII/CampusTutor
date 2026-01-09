/**
 * 地图相关 API (共享模块)
 */
import request from './request';

/**
 * 逆地址解析 (坐标转地址)
 * @param {number} longitude - 经度
 * @param {number} latitude - 纬度
 */
export function reverseGeocode(longitude, latitude) {
  return request.get('/map/geocoder/reverse', {
    params: { longitude, latitude }
  });
}

/**
 * 地址解析 (地址转坐标)
 * @param {string} address - 地址
 */
export function geocode(address) {
  return request.get('/map/geocoder', {
    params: { address }
  });
}

/**
 * 路径规划
 * @param {Object} data - { origin, destination, waypoints }
 */
export function planRoute(data) {
  return request.post('/map/route', data);
}

/**
 * 距离计算
 * @param {Object} params - { origins, destination, type }
 */
export function calculateDistance(params) {
  return request.get('/map/distance', { params });
}

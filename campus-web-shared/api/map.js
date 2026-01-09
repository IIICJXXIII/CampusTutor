/**
 * 地图相关 API (共享模块)
 */
import request from './request';

/**
 * 逆地址解析 (坐标转地址)
 * @param {number} latitude - 纬度
 * @param {number} longitude - 经度
 */
export function reverseGeocode(latitude, longitude) {
  return request.get('/map/geocoder/reverse', {
    params: { latitude, longitude }
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
 * @param {Object} data - { fromLatitude, fromLongitude, toLatitude, toLongitude, mode }
 */
export function planRoute(data) {
  return request.post('/map/direction', data);
}

/**
 * 距离计算
 * @param {Object} params - { fromLatitude, fromLongitude, toLatitude, toLongitude, mode }
 */
export function calculateDistance(params) {
  return request.get('/map/distance', { params });
}

/**
 * 地图服务 API
 * 对应后端: MapController
 */
import request from './request';

/**
 * 逆地址解析 (经纬度 -> 地址)
 * @param {number} latitude 纬度
 * @param {number} longitude 经度
 */
export function reverseGeocode(latitude, longitude) {
  return request.get('/map/geocoder/reverse', {
    params: { latitude, longitude }
  });
}
export function findNearbyDemands(params) {
  return request.get('/demand/nearby', { params });
}
/**
 * 路径规划 (获取路线、距离、耗时)
 * @param {Object} data - { fromLatitude, fromLongitude, toLatitude, toLongitude, mode }
 * mode: walking(默认), driving, transit
 */
export function getDirection(data) {
  return request.post('/map/direction', data);
}

/**
 * 距离计算 (计算两点间直线或步行距离)
 * @param {Object} params - { fromLatitude, fromLongitude, toLatitude, toLongitude, mode }
 */
export function getDistance(params) {
  return request.get('/map/distance', { params });
}
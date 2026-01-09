// 地图工具类 - 封装高德地图API调用
const request = require('./request.js');
const api = require('../config/apiConfig.js');

/**
 * 地图工具类
 * 封装高德地图相关API调用，小程序使用微信内置地图组件配合后端API
 */
const mapUtil = {
  /**
   * 逆地址解析 - 根据经纬度获取地址信息
   * @param {number} latitude 纬度
   * @param {number} longitude 经度
   * @returns {Promise} 解析结果
   */
  async reverseGeocode(latitude, longitude) {
    try {
      const res = await request.get(api.map.reverseGeocode, {
        params: { latitude, longitude }
      });
      return res;
    } catch (err) {
      console.error('逆地址解析失败:', err);
      throw err;
    }
  },

  /**
   * 地址解析 - 根据地址获取经纬度
   * @param {string} address 地址字符串
   * @returns {Promise} 解析结果
   */
  async geocode(address) {
    try {
      const res = await request.get(api.map.geocode, {
        params: { address }
      });
      return res;
    } catch (err) {
      console.error('地址解析失败:', err);
      throw err;
    }
  },

  /**
   * 路径规划 - 获取路线信息
   * @param {Object} data - 路径规划参数
   * @param {number} data.fromLatitude 起点纬度
   * @param {number} data.fromLongitude 起点经度
   * @param {number} data.toLatitude 终点纬度
   * @param {number} data.toLongitude 终点经度
   * @param {string} data.mode 出行方式: walking(默认), driving, transit
   * @returns {Promise} 路径规划结果
   */
  async getDirection(data) {
    try {
      const res = await request.post(api.map.direction, data);
      return res;
    } catch (err) {
      console.error('路径规划失败:', err);
      throw err;
    }
  },

  /**
   * 距离计算 - 计算两点间距离
   * @param {Object} params - 距离计算参数
   * @param {number} params.fromLatitude 起点纬度
   * @param {number} params.fromLongitude 起点经度
   * @param {number} params.toLatitude 终点纬度
   * @param {number} params.toLongitude 终点经度
   * @param {string} params.mode 距离类型: walking(默认), driving
   * @returns {Promise} 距离计算结果
   */
  async getDistance(params) {
    try {
      const res = await request.get(api.map.distance, {
        params
      });
      return res;
    } catch (err) {
      console.error('距离计算失败:', err);
      throw err;
    }
  },

  /**
   * 计算两点间直线距离（备用，当API不可用时使用）
   * @param {number} lat1 起点纬度
   * @param {number} lng1 起点经度
   * @param {number} lat2 终点纬度
   * @param {number} lng2 终点经度
   * @returns {number} 距离（单位：公里）
   */
  calculateStraightDistance(lat1, lng1, lat2, lng2) {
    const radLat1 = lat1 * Math.PI / 180.0;
    const radLat2 = lat2 * Math.PI / 180.0;
    const a = radLat1 - radLat2;
    const b = (lng1 * Math.PI / 180.0) - (lng2 * Math.PI / 180.0);
    let s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
      Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
    s = s * 6378.137; // 地球半径
    return s;
  },

  /**
   * 打开地图查看位置
   * @param {Object} location - 位置信息
   * @param {string} location.name 位置名称
   * @param {number} location.latitude 纬度
   * @param {number} location.longitude 经度
   */
  openMap(location) {
    wx.openLocation({
      name: location.name,
      latitude: location.latitude,
      longitude: location.longitude,
      scale: 15
    });
  },

  /**
   * 选择位置
   * @returns {Promise} 选择结果
   */
  chooseLocation() {
    return new Promise((resolve, reject) => {
      wx.chooseLocation({
        success(res) {
          resolve(res);
        },
        fail(err) {
          console.error('选择位置失败:', err);
          reject(err);
        }
      });
    });
  }
};

module.exports = mapUtil;

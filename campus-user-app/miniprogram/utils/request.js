// utils/request.js
const { baseUrl } = require('../config/apiConfig.js');

/**
 * 统一网络请求封装
 * @param {string} url 接口路径
 * @param {string} method 请求方法 GET/POST/PUT/DELETE
 * @param {object} data 请求参数
 * @param {boolean} showLoading 是否显示加载loading
 */
const request = (url, method = 'GET', data = {}, showLoading = true) => {
  if (showLoading) {
    wx.showLoading({ title: '加载中...', mask: true });
  }

  return new Promise((resolve, reject) => {
    // 获取本地存储的Token [cite: 443]
    const token = wx.getStorageSync('token');
    
    wx.request({
      url: baseUrl + url,
      method: method,
      data: data,
      header: {
        'content-type': 'application/json',
        // 注入JWT Token用于后端鉴权
        'Authorization': token ? `Bearer ${token}` : '' 
      },
      success: (res) => {
        if (showLoading) wx.hideLoading();
        
        // 依据HTTP状态码或业务Code判断
        const { code, msg, data: responseData } = res.data;
        
        if (code === 200) {
          resolve(responseData);
        } else if (code === 401) {
          // Token过期或未登录，跳转至登录页 
          wx.showToast({ title: '登录已过期', icon: 'none' });
          setTimeout(() => {
            wx.reLaunch({ url: '/pages/common/login/login' });
          }, 1500);
          reject(res.data);
        } else {
          // 业务错误提示
          wx.showToast({ title: msg || '服务器繁忙', icon: 'none' });
          reject(res.data);
        }
      },
      fail: (err) => {
        if (showLoading) wx.hideLoading();
        wx.showToast({ title: '网络连接异常', icon: 'none' });
        reject(err);
      }
    });
  });
};

module.exports = {
  get: (url, data, loading) => request(url, 'GET', data, loading),
  post: (url, data, loading) => request(url, 'POST', data, loading),
  put: (url, data, loading) => request(url, 'PUT', data, loading),
  del: (url, data, loading) => request(url, 'DELETE', data, loading)
};
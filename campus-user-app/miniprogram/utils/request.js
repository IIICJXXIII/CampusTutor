import apiConfig from '../config/apiConfig';

const request = (url, method = 'GET', data = {}, header = {}) => {
  return new Promise((resolve, reject) => {
    // 1. 获取本地存储的 Token
    const token = wx.getStorageSync('token');
    
    // 2. 组装 Header
    const defaultHeader = {
      'content-type': 'application/json',
      ...header
    };
    
    if (token) {
      defaultHeader['Authorization'] = `Bearer ${token}`;
    }

    // 3. 发起请求
    wx.request({
      url: url,
      method: method,
      data: data,
      header: defaultHeader,
      timeout: 10000,
      success: (res) => {
        const { statusCode, data: resData } = res;
        
        // HTTP 状态码判断
        if (statusCode >= 200 && statusCode < 300) {
          // 【核心修改】：兼容 code=200 和 code=0 两种成功状态
          if (resData.code === 200 || resData.code === 0) {
            resolve(resData.data);
          } else {
            // 业务错误 (如 401 未授权)
            if (resData.code === 401) {
              wx.showToast({ title: '登录已过期', icon: 'none' });
              wx.removeStorageSync('token');
              wx.removeStorageSync('userInfo');
              setTimeout(() => {
                wx.reLaunch({ url: '/pages/common/login/login' });
              }, 1500);
            } else {
              wx.showToast({ title: resData.msg || '请求失败', icon: 'none' });
            }
            reject(resData);
          }
        } else {
          // HTTP 错误
          wx.showToast({ title: `网络错误 ${statusCode}`, icon: 'none' });
          reject(res);
        }
      },
      fail: (err) => {
        wx.showToast({ title: '网络连接失败', icon: 'none' });
        reject(err);
      }
    });
  });
};

// 导出快捷方法
export default {
  get: (url, data) => request(url, 'GET', data),
  post: (url, data) => request(url, 'POST', data),
  put: (url, data) => request(url, 'PUT', data),
  delete: (url, data) => request(url, 'DELETE', data),
  
  // 文件上传封装
  upload: (url, filePath, formData = {}) => {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token');
      wx.uploadFile({
        url: url,
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': token ? `Bearer ${token}` : ''
        },
        formData: formData,
        success: (res) => {
          // uploadFile 返回的 data 是字符串，需要 parse
          const data = JSON.parse(res.data);
          // 同样兼容 200 和 0
          if (data.code === 200 || data.code === 0) {
            resolve(data.data);
          } else {
            wx.showToast({ title: data.msg || '上传失败', icon: 'none' });
            reject(data);
          }
        },
        fail: (err) => {
          reject(err);
        }
      });
    });
  }
};
const apiConfig = require('../config/apiConfig.js');

const request = (url, method = 'GET', data = {}, header = {}) => {
  return new Promise((resolve, reject) => {
    // 1. 获取本地存储的 Token
    const token = wx.getStorageSync('token');
    
    // 【调试日志】(发布时可注释) 
    // 观察这里打印的是什么，如果是 undefined/null，说明注册/登录时没存对
    if (url.includes('certification')) {
        console.log('==== 请求调试 ====');
        console.log('URL:', url);
        console.log('本地Token:', token);
    }

    // 2. 组装 Header
    const defaultHeader = {
      'content-type': 'application/json',
      ...header
    };
    
    // 【关键修复】确保 Token 有效才添加 Header
    // 防止发送 "Bearer undefined" 导致后端解析崩溃
    if (token && token !== 'undefined' && token !== 'null') {
      // 注意：这里中间必须有一个空格
      defaultHeader['Authorization'] = `Bearer ${token}`; 
    } else {
      // 如果是提交认证这种关键接口缺 Token，在控制台报个警
      if (url.includes('certification')) {
        console.warn('【警告】正在发起认证请求，但请求头中缺失 Token！');
      }
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
          // 兼容 code=200 和 code=0
          if (resData.code === 200 || resData.code === 0) {
            resolve(resData.data);
          } else {
            // 401 未授权处理
            if (resData.code === 401) {
              wx.showToast({ title: '登录状态已失效', icon: 'none' });
              // 清除脏数据
              wx.removeStorageSync('token');
              wx.removeStorageSync('userInfo');
              // 延迟跳转登录
              setTimeout(() => {
                wx.reLaunch({ url: '/pages/common/login/login' });
              }, 1500);
            } else {
              wx.showToast({ title: resData.msg || '请求失败', icon: 'none' });
            }
            reject(resData);
          }
        } else {
          console.error('HTTP Error:', statusCode, resData);
          wx.showToast({ title: `服务异常 ${statusCode}`, icon: 'none' });
          reject(res);
        }
      },
      fail: (err) => {
        console.error('Network Error:', err);
        wx.showToast({ title: '网络连接失败', icon: 'none' });
        reject(err);
      }
    });
  });
};

// 导出快捷方法
module.exports = {
  get: (url, data) => request(url, 'GET', data),
  post: (url, data) => request(url, 'POST', data),
  put: (url, data) => request(url, 'PUT', data),
  delete: (url, data) => request(url, 'DELETE', data),
  
  // 文件上传封装
  upload: (url, filePath, formData = {}) => {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token');
      const header = {};
      if (token && token !== 'undefined') {
        header['Authorization'] = `Bearer ${token}`;
      }

      wx.uploadFile({
        url: url,
        filePath: filePath,
        name: 'file',
        header: header,
        formData: formData,
        success: (res) => {
          // uploadFile 返回的 data 是字符串，需要 parse
          try {
            const data = JSON.parse(res.data);
            if (data.code === 200 || data.code === 0) {
              resolve(data.data);
            } else {
              wx.showToast({ title: data.msg || '上传失败', icon: 'none' });
              reject(data);
            }
          } catch (e) {
            reject(res.data);
          }
        },
        fail: (err) => {
          reject(err);
        }
      });
    });
  }
};